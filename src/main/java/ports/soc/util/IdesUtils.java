package ports.soc.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;

import ports.soc.ides.model.DataModel;

/**
 * General utility class for web application
 * 
 * @author WIPU
 *
 */
public class IdesUtils {

	// public static final String EM_DASH = "–";

	public static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private static final Random RANDOM = new Random();
	
	/**
	 * Fix issue: PrimeFaces's TextEditor does not handle \r\n properly
	 * @param txt
	 * @return
	 */
	public static String replaceNewlineToBr(String txt) {
		if (IdesUtils.isEmpty(txt)) {
			return txt;
		}
		return txt.replaceAll("(\n)|(\r\n)", "<br/>");
	}
	
	public static boolean randomIntValue(int i) {
		if (RANDOM.nextInt(i) % 2 == 0) {
			return true;
		}
		return false;
	}
	
	public static String trim(String s) {
		if (s == null) {
			return null;
		}
		return s.trim();
	}
	
	public static boolean isNull(Object obj) {
		return obj == null ? true : false;
	}

	public static boolean isEmpty(String str) {
		if (isNull(str)) {
			return true;
		}
		return str.trim().isEmpty();
	}
	
	public static boolean isEmpty(Collection<?> collection) {
		if (isNull(collection)) {
			return true;
		}
		return collection.isEmpty();
	}

	public static boolean isEmpty(Object[] object) {
		if (isNull(object)) {
			return true;
		}
		for (Object o : object) {
			if (!isNull(o)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static String deepPrint(Object o) {
		if (o == null) {
			return "null";
		}
		if (o instanceof Object[]) {
			Object[] oo = (Object[]) o;
			return print(oo);
		}
		if (o instanceof Map) {
			Map m = (Map) o;
			return print(m);
		}
		if (o instanceof Collection) {
			Collection c = (Collection) o;
			return print(c);
		}
		if (o instanceof Enumeration) {
			Enumeration e = (Enumeration) o;
			return print(e);
		}
		if (o instanceof DataModel) {
			DataModel m = (DataModel) o;
			return DataModel.printDetail(m);
		}
		if (o instanceof ActionEvent) {
			ActionEvent ae = (ActionEvent) o;
			return print(ae);
		}
		return String.valueOf(o);
	}
	
	public static String print(ActionEvent ae) {
		if (ae == null) {
			return "null";
		}
		UIComponent component = ae.getComponent();
		StringBuilder sb = new StringBuilder();
		sb.append("component[");
		if (component != null) {
			sb.append("type=").append(component.getClass().getSimpleName())
			.append(", family=").append(component.getFamily())
			.append(", clientId=").append(component.getClientId());
		}
		sb.append("]");
		return sb.toString();
	}
	
	public static String print(Object[] obj) {
		if (obj == null) {
			return "null";
		}
		if (obj.length <= 0) {
			return "{}";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(int i = 0; i < obj.length; i++) {
			sb.append(deepPrint(obj[i]));
			if (i + 1 < obj.length) {
				sb.append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	public static String print(Enumeration e) {
		if (e == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		while(e.hasMoreElements()) {
			Object o = e.nextElement();
			sb.append(deepPrint(o));
			sb.append(", ");
		}
		sb.append("}");
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	public static String print(Map map) {
		if (map == null) {
			return "null";
		}
		if (map.isEmpty()) {
			return "{}";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			Object value = map.get(key);
			sb.append(deepPrint(key));
			sb.append("=");
			sb.append(deepPrint(value));
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public static String print(Collection<?> collection) {
		if (collection == null) {
			return "null";
		}
		if (collection.isEmpty()) {
			return "{}";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		Iterator<?> it = collection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			sb.append(deepPrint(o));
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public static boolean isValidEmail(String email) {
		return email.matches(EMAIL_REGEX);
	}

	public static String printDetail(DataModel model) {
		if (isNull(model)) {
			return "null";
		}
		return model.printDetail();
	}
	
}
