package salesmachine.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stevesoft.pat.Transformer;

public class Filter {

	private static final Logger log = LoggerFactory.getLogger(Filter.class);

	private final String string;
	//private Regex reg;
	private static Transformer regularYahooTransformer;
	private static Transformer imageYahooTransformer;
	private static Transformer jpgTransformer;

	private static void applyYahooTransformations(Transformer aTransformer) {
		aTransformer.add("s/ //g");
		aTransformer.add("s/\\./-/g");
		aTransformer.add("s/\"/-/g");
		aTransformer.add("s/\\*/-/g");
		aTransformer.add("s/\\//-/g");
		aTransformer.add("s/,//g");
		aTransformer.add("s/\\(//g");
		aTransformer.add("s/\\)//g");
		aTransformer.add("s/#/-/g");
		aTransformer.add("s/\\&/-/g");
		aTransformer.add("s/\\+/-/g");
		aTransformer.add("s/\\\\/-/g");
		aTransformer.add("s/" + (char) 187 + "//g");
		aTransformer.add("s/" + (char) 188 + "//g"); // 1/4
		aTransformer.add("s/" + (char) 189 + "//g"); // 1/2
		aTransformer.add("s/" + (char) 190 + "//g"); // 3/4
		aTransformer.add("s/" + (char) 191 + "//g");
	}

	public Filter(String string_) {
		string = string_;
	}

	public static String transForm(String tr, String str) {
		Transformer trans = new Transformer(true);
		trans.add(tr);
		str = trans.replaceAll(str);
		return str;
	}

	// filter a sku to be acceptable as a yahoo image name
	public static String filterYahooSku(String sku) {
		return salesmachine.util.Filter.filterYahooSku(sku, false);
	}

	// filter a sku to be acceptable as a yahoo image name
	public static String filterYahooSku(String sku, boolean forImage) {
		synchronized (Filter.class) {
			if (regularYahooTransformer == null) {
				regularYahooTransformer = new Transformer(true);
				applyYahooTransformations(regularYahooTransformer);
				regularYahooTransformer.add("s/-/_/g");
			}
			if (imageYahooTransformer == null) {
				imageYahooTransformer = new Transformer(true);
				applyYahooTransformations(imageYahooTransformer);
				imageYahooTransformer.add("s/_/-/g");
			}
			if (jpgTransformer == null) {
				jpgTransformer = new Transformer(true);
				jpgTransformer.add("s/-jpg$/\\.jpg/i");
			}
			if (forImage) {
				sku = imageYahooTransformer.replaceAll(sku);
			} else {
				sku = regularYahooTransformer.replaceAll(sku);
			}
			sku = jpgTransformer.replaceAll(sku);
			return sku;
		}
	}

	// This replaces all "'" with "''" for oracle inserts and updates
	public static String filterSingleQuotes(String str) {
		String returnStr = "";
		if (str == null) {
			return null;
		}
		if (str.startsWith("'")) {
			returnStr = "''";
		}
		try {
			java.util.StringTokenizer st = new java.util.StringTokenizer(str,
					"'");
			int ind = 0;
			while (st.hasMoreTokens()) {
				String word = st.nextToken();
				returnStr += word + "''";
				ind++;
			}
			if (ind == 1) {
				returnStr = returnStr.substring(0, returnStr.length() - 2);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return returnStr;
	}

	public static char[] convertCharArrayToUnicode(char[] array) {
		StringBuffer sb = new StringBuffer(
				java.lang.reflect.Array.getLength(array));
		// int oldVal = 0;
		for (int i = 0; i < java.lang.reflect.Array.getLength(array); i++) {
			int val = (int) array[i];
			if (val < 7 || val == 11 || val == 12 || (val > 13 && val < 32)
					|| val > 126 && val != 194 && val <= 256) {
				sb.append("&#" + (int) (array[i]) + ";");
			} else if (val == 8482) {
				sb.append("&#153;");
			} else if (val == 194 || val > 256) {
			} else {
				if (val != 13) {
					sb.append(array[i]);
				}
			}
			// oldVal = val;
		}
		return sb.toString().toCharArray();
	}

	public static String filterHTML(String stringToFilter) {
		String filteredString = stringToFilter;

		filteredString = Filter.transForm("s/\n//gi", filteredString);
		filteredString = Filter.transForm("s/\r//gi", filteredString);
		filteredString = Filter.transForm("s/ +/ /gi", filteredString);
		filteredString = Filter.transForm("s/<p.*?>//gi", filteredString);
		filteredString = Filter.transForm("s/<\\/p>/    /gi", filteredString);
		filteredString = Filter.transForm("s/<br>/  /gi", filteredString);
		filteredString = Filter.transForm("s/<ul>.+?<li>//gi", filteredString);
		filteredString = Filter.transForm("s/<li>/, /gi", filteredString);
		filteredString = Filter.transForm("s/<\\/UL>//gi", filteredString);
		filteredString = Filter.transForm("s/<\\/pre>//gi", filteredString);
		filteredString = Filter.transForm("s/<.+?>//gi", filteredString);
		// /////////////////////////////
		// BY ANOOP on 1/6/2005
		// ///////////////////////////
		filteredString = Filter.transForm("s/ class=?.+??//g", filteredString);

		// //////////////////////////

		if (filteredString.startsWith(", ")) {
			filteredString = filteredString.substring(2,
					filteredString.length());
		} else if (filteredString.startsWith(" , ")) {
			filteredString = filteredString.substring(3,
					filteredString.length());
		}
		filteredString = Filter.transForm("s/\\.\\./\\./gi", filteredString);

		return filteredString;
	}

	public static String filterxml(String datatofilter) {
		String filteredstring = "";
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < datatofilter.length(); i++) { // for reading one by
															// one charactor
															// from string
			char c = datatofilter.charAt(i);

			if (Character.isLetterOrDigit(c)) {
				result.append(c);
			}

		}// end of for loop
		return result.toString();
	}
}
