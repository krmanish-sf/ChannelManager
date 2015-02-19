package com.inventorysource.cm.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.FormObject;

import com.inventorysource.cm.web.config.ApplicationProperties;

public class SignUpHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 7483848858978782601L;
	private static final Logger LOG = LoggerFactory
			.getLogger(SignUpHandlerServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String cmd = req.getParameter("cmd");
		if ("signup".equalsIgnoreCase(cmd)) {
			signUpStep1(req, resp);
		} else if ("payment".equalsIgnoreCase(cmd)) {
			signUpStep2(req, resp);
		}
	}

	private void signUpStep2(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		// Process the billing of $500 on the credit card
		Payments.CreditCard cc = new Payments.CreditCard();
		cc.card_num = request.getParameter("credit_card");
		cc.exp_month = request.getParameter("credit_card_exp_mon");
		cc.exp_year = request.getParameter("credit_card_exp_year");
		cc.type = request.getParameter("credit_card_type");
		cc.name_on_card = request.getParameter("name_on_card");

		Payments.Contact contact = new Payments.Contact();
		HttpSession session = request.getSession();
		Reps r = (Reps) session.getAttribute("rep");
		if (r == null) {
			contact.firstName = (String) session.getAttribute("first_name");
			contact.lastName = (String) session.getAttribute("last_name");
			contact.email = (String) session.getAttribute("email");
			contact.phone = (String) session.getAttribute("phone");
			contact.address1 = (String) session.getAttribute("company_name");
		} else {
			contact.firstName = r.getFirstName();
			contact.lastName = r.getLastName();
			contact.email = r.getLogin();
			contact.phone = "";
			contact.address1 = "";
		}

		contact.address2 = "";
		contact.city = "";
		contact.state = "";
		contact.zip = "";
		contact.province = "";
		contact.country = "";

		double amountToCharge = 2.5;
		// If the transaction is successful, create the account and get the user
		// logged in
		// If the transaction fails, send back to the billing screen and show
		// the error
		Payments payment = new Payments();
		if (payment.chargeVendor(cc, contact, amountToCharge,
				"Inventory Source Channel Manager Setup Charge")) {
			String pageResponseString = "";
			String login, password = "";
			if (r == null) {
				// Do the registration
				String[] params = new String[] { "first_name",
						contact.firstName, "last_name", contact.lastName,
						"phone", contact.phone, "email", contact.email,
						"password", (String) session.getAttribute("password"),
						"company_name",
						(String) session.getAttribute("company_name"),
						"ccType", cc.type, "ccNum", cc.card_num, "ccExpMonth",
						cc.exp_month, "ccExpYear", cc.exp_year, "nameOnCard",
						cc.name_on_card, "amount", amountToCharge + "",
						"cmRegistrationForm", "true" };
				FormObject formObj = new FormObject("www.inventorysource.com",
						80, "/KBlistener", "", "", false, false, false);
				if (params != null && params.length > 0) {
					formObj.addData(params);
				}
				formObj.setTimeOut(60 * 1000 * 15);
				formObj.hitForm("Post", null);
				pageResponseString = formObj.page;
				login = contact.email;
				password = (String) session.getAttribute("password");
			} else {
				// Do the activation
				String[] params = new String[] { "vendor_id",
						r.getVendorId() + "", "ccType", cc.type, "ccNum",
						cc.card_num, "ccExpMonth", cc.exp_month, "ccExpYear",
						cc.exp_year, "nameOnCard", cc.name_on_card, "amount",
						amountToCharge + "", "cmActivationForm", "true" };
				FormObject formObj = new FormObject("www.inventorysource.com",
						80, "/KBlistener", "", "", false, false, false);
				if (params != null && params.length > 0) {
					formObj.addData(params);
				}
				formObj.setTimeOut(60 * 1000 * 15);
				formObj.hitForm("Post", null);
				pageResponseString = formObj.page;
				login = r.getLogin();
				password = r.getPassword();
			}

			Map<String, String> loginDetails = new HashMap<String, String>();
			loginDetails.put("username", login);
			loginDetails.put("password", password);
			RestTemplate template = new RestTemplate();
			ResponseEntity<Reps> entity = template.postForEntity(
					ApplicationProperties.getRestServiceUrl() + "login",
					loginDetails, salesmachine.hibernatedb.Reps.class);
			Reps reps = entity.getBody();
			session.setAttribute("reps", reps);
			response.sendRedirect("index.jsp");
			SessionManager.closeSession();
		} else {
			request.setAttribute("error",
					"Could not process the charge. Error: " + payment.message);
			RequestDispatcher rd = request.getServletContext()
					.getRequestDispatcher("/payment.jsp");
			rd.include(request, response);
		}
	}

	private void signUpStep1(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		// Put everything in session
		// If the IS login method used, verify the login
		// if its valid, store the VID in session
		// else, send back to the page with the error displayed
		// If the new account method is used, verify if the email already exists
		// if it exists, send back to the page with the error
		// if not, store all values in the session
		Session dbSession = SessionManager.currentSession();
		HttpSession session = request.getSession();

		String email = request.getParameter("email");
		if (uniqueEmail(dbSession, email)) {
			session.setAttribute("email", email);
			session.setAttribute("first_name",
					request.getParameter("first_name"));
			session.setAttribute("last_name", request.getParameter("last_name"));
			session.setAttribute("company_name",
					request.getParameter("company_name"));
			session.setAttribute("phone", request.getParameter("phone"));
			session.setAttribute("password", request.getParameter("password"));
			response.sendRedirect("/payment.jsp");
		} else {
			request.setAttribute(
					"error",
					"This email is already registered. Don't remember the account details? <a href='/members/oimhome.action?cmd=resend&email="
							+ email
							+ "'>Click here</a> to receive login details on your email.");
			RequestDispatcher rd = request.getServletContext()
					.getRequestDispatcher("/signup.jsp");
			rd.include(request, response);
		}
		SessionManager.closeSession();
	}

	static boolean uniqueEmail(Session dbSession, String login) {
		Transaction tx = null;
		Reps r = null;
		try {
			tx = dbSession.beginTransaction();
			// Here is your db code
			Query query = dbSession
					.createQuery("from salesmachine.hibernatedb.Reps r where r.login=:login");
			query.setString("login", login);
			Iterator it = query.iterate();
			if (it.hasNext()) {
				r = (salesmachine.hibernatedb.Reps) it.next();
			}
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error(e.getMessage(), e);
		}
		return (r == null);
	}
}
