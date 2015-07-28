package salesmachine.util;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberFormat {

	private static final Logger log = LoggerFactory
			.getLogger(NumberFormat.class);

	public NumberFormat() {
	}

	public static double roundDouble(double val) {
		double left = (int) val;
		double right = val - left;
		right *= 100;
		right = Math.round(right);
		val = left + (right / 100);
		return val;
	}

	public static float roundFloat(float val) {
		float left = (int) val;
		float right = val - left;
		right *= 100f;
		int rightI = Math.round(right);
		val = left + ((float) rightI / 100f);
		return val;
	}

	public static String formatNum(int val) {
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");
		String formatted_val = formatter.format((double) val);
		return formatted_val;
	}

	public static String formatNum(String val) {
		try {
			val = StringHandle.removeNull(val);
			val = Filter.transForm("s/,//g", val);
			if (val.length() > 0) {
				return formatNum(Double.parseDouble(val));
			} else {
				return "";
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "";
		}
	}

	public static String formatNum(double val) {
		DecimalFormat formatter = new DecimalFormat("#,###,###,##0.00");
		val = NumberFormat.roundDouble(val);
		String formatted_val = formatter.format(val);
		return formatted_val;
	}

	public static String formatNum(float val) {
		DecimalFormat formatter = new DecimalFormat("#,###,###,##0.00");
		val = NumberFormat.roundFloat(val);
		String formatted_val = formatter.format(val);
		return formatted_val;
	}

}
