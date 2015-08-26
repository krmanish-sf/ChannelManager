package salesmachine.util;

import com.stevesoft.pat.Transformer;

public class StringHandle {
	private static Transformer bastardCharsTransformer = null;

	private static void buildBastardCharsTransformer() {
		Transformer trans = new Transformer(true);
		trans.add("s/" + (char) 65533 + "/ /gi");
		trans.add("s/\\" + (char) 47 + "/\\//gi");
		trans.add("s/\\" + (char) 25 + "/\\//gi");
		trans.add("s/" + (char) 11 + "/<br>/gi"); // added by Hambir Singh on 16
													// Jun 2008
		trans.add("s/\\" + (char) 2 + "/\\//gi");
		trans.add("s/\\" + (char) 28 + "/\\//gi");
		trans.add("s/\\" + (char) 29 + "/\\//gi");
		trans.add("s/" + (char) 213 + "/'/gi");
		trans.add("s/" + (char) 174 + "/&reg;/gi");
		trans.add("s/" + (char) 168 + "/&reg;/gi");
		trans.add("s/" + (char) 169 + "/&copy;/gi");
		trans.add("s/" + (char) 153 + "/&trade;/gi");
		trans.add("s/" + (char) 170 + "/&trade;/gi");
		trans.add("s/" + (char) 8482 + "/&trade;/gi");
		trans.add("s/" + (char) 165 + "/&middot;/gi");
		trans.add("s/" + (char) 189 + "/&frac12;/gi");
		trans.add("s/" + (char) 188 + "/&frac14;/gi");
		trans.add("s/" + (char) 233 + "/e/gi"); // ?
		trans.add("s/" + (char) 201 + "/E/gi"); // ?
		trans.add("s/" + (char) 183 + "/ /gi");
		trans.add("s/" + (char) 178 + "/&#xB2;/gi");
		trans.add("s/" + (char) 177 + "/&#xB1;/gi");
		trans.add("s/" + (char) 161 + "/&#xA1;/gi");
		trans.add("s/" + (char) 92 + "/ /gi");
		trans.add("s/" + (char) 176 + "/&#xB0;/gi");
		trans.add("s/" + (char) 8225 + "/&Dagger;/gi");
		trans.add("s/" + (char) 8226 + "/&bull;/gi");
		trans.add("s/" + (char) 160 + "/&#xA0;/gi");
		trans.add("s/" + (char) 215 + "/&#xD7;/gi");
		trans.add("s/" + (char) 173 + "/&#xAD;/gi");
		trans.add("s/" + (char) 96 + "/ /gi");
		trans.add("s/" + (char) 191 + "/&#xBF;/gi");
		trans.add("s/" + (char) 189 + "/&#xBD;/gi");
		trans.add("s/" + (char) 169 + "/&#xA9;/gi");
		trans.add("s/" + (char) 8217 + "/&rsquo;/gi");
		trans.add("s/" + (char) 8216 + "/&lsquo;/gi");
		trans.add("s/" + (char) 8220 + "/&ldquo;/gi");
		trans.add("s/" + (char) 8221 + "/&rdquo;/gi");
		trans.add("s/" + (char) 8211 + "/&ndash;/gi");
		trans.add("s/" + (char) 8212 + "/&mdash;/gi");
		trans.add("s/" + (char) 8224 + "/&dagger;/gi");
		trans.add("s/" + (char) 8230 + "/&hellip;/gi");
		trans.add("s/" + (char) 185 + "/&#xB9;/gi");
		bastardCharsTransformer = trans;
	}

	public static String removeNull(String value) {
		if (value == null || value.equals("null")) {
			return "";
		}
		return value;
	}

	public static String removeNull(Object obj) {
		String value = null;
		if (obj == null)
			value = "";
		else
			value = obj.toString().trim();
		return value;
	}

	public static String maxSize(String str, int size) {
		if (str != null) {
			if (str.length() > size) {
				str = str.substring(0, size);
			}
		}
		return str;
	}

	public static String removeBastardCharacters(String page) {
		synchronized (StringHandle.class) {
			if (bastardCharsTransformer == null) {
				buildBastardCharsTransformer();
			}
			return bastardCharsTransformer.replaceAll(page);
		}
	}

	public static String formatTitleCase(String str) {
		if (str != null && str.length() > 0) {
			str = str.toLowerCase();
			// remove leading spaces
			while (str.startsWith(" "))
				str = str.substring(1);
			// remove trailing spaces
			while (str.endsWith(" "))
				str = str.substring(0, (str.length() - 1));
			boolean followingSpace = false;
			char[] c_array = str.toCharArray();
			for (int index = 0; index < c_array.length; index++) {
				if (index == 0 || followingSpace) {
					if (c_array[index] == ' ')
						continue;
					c_array[index] = Character.toUpperCase(c_array[index]);
				}
				followingSpace = false;
				if (c_array[index] == ' ') {
					followingSpace = true;
				}
			}
			str = new String(c_array);
		}
		return str;
	}

	public static String transForm(String tr, String str) {
		Transformer trans = new Transformer(true);
		trans.add(tr);
		str = trans.replaceAll(str);
		return str;
	}

	public static boolean isNullOrEmpty(final String str) {
		return str == null || str.isEmpty();
	}
	
	public static String removeComma(String str){
		str = removeNull(str);
		str = str.replaceAll(",", " ");
		return str;
	}
}
