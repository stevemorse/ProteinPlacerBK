package annotation;

import java.util.Map;

public interface IAnnotation extends Map<String, String>{
	
	public String getAnnotationKey();
	public void setAnnotationKey(String key);
	public String getAnnotationValue();
	public void setAnnotationValue(String Value);
}
