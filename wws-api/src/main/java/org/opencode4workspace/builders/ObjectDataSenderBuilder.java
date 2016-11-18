package org.opencode4workspace.builders;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.opencode4workspace.WWException;
import org.opencode4workspace.bo.PageInfo;
import org.opencode4workspace.bo.WWFieldsAttributesInterface;
import org.opencode4workspace.graphql.builders.GraphQLJsonPropertyHelper;

/**
 * @author Paul Withers
 * @since 0.5.0
 * 
 *        DataSender for sending GraphQL query objects. The object will have a name, may have attributes (parameter to filter the query), fields and/or children to return. Each child will be an
 *        ObjectDataSenderBuilder of its own. Consult WWS Graph QL Builder for details of what attributes etc at https://workspace.ibm.com/graphql
 *
 */
public class ObjectDataSenderBuilder implements DataSenderBuilder, Serializable {

	private static final long serialVersionUID = 1L;
	private String objectName;
	private boolean hasItems;
	private Map<String, Object> attributesList = new HashMap<String, Object>();
	private List<String> fieldsList = new ArrayList<String>();
	private List<DataSenderBuilder> children = new ArrayList<DataSenderBuilder>();
	private ObjectDataSenderBuilder pageInfo;

	/**
	 * Raw constructor. Recommendation is to use overloaded methods
	 */
	public ObjectDataSenderBuilder() {

	}

	/**
	 * Base constructor, defining the object name. Sets hasItems = false.
	 * 
	 * @param objectName
	 *            String name of the object, used in the query. Consult WWS GraphQL Builder for details
	 */
	public ObjectDataSenderBuilder(String objectName) {
		this(objectName, false);
	}

	/**
	 * Base constructor, defining the object name and specifying whether fields / children are in an items object
	 * 
	 * @param objectName
	 *            String name of the object, used in the query. Consult WWS GraphQL Builder for details
	 * @param hasItems
	 *            boolean whether the fields / children are wrapped in "items{}"
	 */
	public ObjectDataSenderBuilder(String objectName, boolean hasItems) {
		this.objectName = objectName;
		this.hasItems = hasItems;
	}

	/**
	 * Constructor, defining the object name, specifying whether fields are in an items object. All fields are then added using the properties of the class passed. NOTE: this will not work for classes
	 * that are intended to contain children, only scalar field values.
	 * 
	 * @param objectName
	 *            String name of the object, used in the query. Consult WWS GraphQL Builder for details
	 * @param clazz
	 *            Class from which to add all fields
	 * @param hasItems
	 *            boolean whether the fields / children are wrapped in "items{}"
	 */
	public ObjectDataSenderBuilder(String objectName, Class<?> clazz, Boolean hasItems) {
		this.objectName = objectName;
		this.hasItems = hasItems;
		for (Field f : clazz.getDeclaredFields()) {
			if (!Modifier.isStatic(f.getModifiers())) {
				String fieldName = extractFieldName(f);
				fieldsList.add(fieldName);
			}
		}
	}

	/**
	 * Constructor, defining the object name. All fields are then added using the enum passed. Children can then be added subsequently. {@link #isHasItems()} will be false
	 * 
	 * @param objectName
	 *            String name of the object, used in the query. Consult WWS GraphQL Builder for details
	 * @param fieldsEnum
	 *            Enum of fields, all of which will be added
	 */
	public ObjectDataSenderBuilder(String objectName, WWFieldsAttributesInterface[] fieldsEnum) {
		this(objectName, false, fieldsEnum);
	}

	/**
	 * Constructor, defining the object name and whether fields should be wrapped in an items object. All fields are then added using the enum passed. Children can then be added subsequently.
	 * 
	 * @param objectName
	 *            String name of the object, used in the query. Consult WWS GraphQL Builder for details
	 * @param hasItems
	 *            boolean whether the fields / children are wrapped in "items{}"
	 * @param fieldsEnum
	 *            Enum of fields, all of which will be added
	 */
	public ObjectDataSenderBuilder(String objectName, boolean hasItems, WWFieldsAttributesInterface[] fieldsEnum) {
		this.objectName = objectName;
		this.hasItems = hasItems;
		for (WWFieldsAttributesInterface f : fieldsEnum) {
			fieldsList.add(f.getLabel());
		}
	}

	/**
	 * Extracts a field name from a class's property. If the {@linkplain GraphQLJsonPropertyHelper} annotation is present, instead of using the class's property name, the annotation label will be used
	 * instead. This is for e.g. "conversationContent" property, which maps to "conversation" field in GraphQL query, but cannot have that property name in this API.
	 * 
	 * @param f
	 *            Field property of a class
	 * @return String property name or annotation
	 */
	private String extractFieldName(Field f) {
		String fieldName = f.getName();
		if (f.isAnnotationPresent(GraphQLJsonPropertyHelper.class)) {
			return f.getAnnotation(GraphQLJsonPropertyHelper.class).jsonProperty();
		}
		return fieldName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opencode4workspace.builders.DataSenderBuilder#build()
	 */
	@Override
	public String build() {
		return build(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opencode4workspace.graphql.builders.DataBringer#buildQuery(boolean)
	 */
	@Override
	public String build(boolean pretty) {
		boolean isFirst = true;
		StringBuilder s = new StringBuilder();
		// Add object name
		s.append(objectName + " ");

		// Add attributes, if exist
		if (!attributesList.isEmpty()) {
			s.append("(");
			for (String key : attributesList.keySet()) {
				if (!isFirst) {
					s.append(" ");
				} else {
					isFirst = false;
				}
				s.append(key + ": ");
				Object obj = attributesList.get(key);
				if (obj instanceof Date) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
					Date dt = (Date) obj;
					TimeZone tz = TimeZone.getDefault();
					df.setTimeZone(tz);
					s.append(df.format(dt));
				} else {
					s.append(obj);
				}
			}
			s.append(") ");
		}

		// Open object
		s.append("{");
		if (pretty) {
			s.append("\n\r");
		}

		// Add pageInfo, if required
		if (null != pageInfo) {
			s.append(pageInfo.build(pretty) + " ");
		}

		// Open "items" container, if required
		if (isHasItems()) {
			s.append("items {");
			if (pretty) {
				s.append("\n\r");
			}
		}

		// Add fields
		isFirst = true;
		for (Object field : fieldsList) {
			if (!isFirst) {
				if (pretty) {
					s.append("\n\r");
				} else {
					s.append(" ");
				}
			} else {
				isFirst = false;
			}
			s.append(field);
		}

		// Add children, if exist
		for (DataSenderBuilder child : getChildren()) {
			if (!isFirst) {
				if (pretty) {
					s.append("\n\r");
				} else {
					s.append(" ");
				}
			} else {
				isFirst = false;
			}
			s.append(child.build(pretty));
		}

		// Close "items" container, if required
		if (isHasItems()) {
			if (pretty) {
				s.append("\n\r");
			}
			s.append("}");
		}

		// Close Object
		if (pretty) {
			s.append("\n\r");
		}
		s.append("}");
		return s.toString();
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public boolean isHasItems() {
		return hasItems;
	}

	public ObjectDataSenderBuilder setHasItems(boolean hasItems) {
		this.hasItems = hasItems;
		return this;
	}

	public List<String> getFieldsList() {
		return fieldsList;
	}

	public void setFieldsList(List<String> fieldsList) {
		this.fieldsList = fieldsList;
	}

	public ObjectDataSenderBuilder addField(String field) {
		fieldsList.add(field);
		return this;
	}

	public ObjectDataSenderBuilder removeField(String field) {
		fieldsList.remove(field);
		return this;
	}

	public Map<String, Object> getAttributesList() {
		return attributesList;
	}

	public ObjectDataSenderBuilder setAttributesList(Map<String, Object> attributesList) {
		this.attributesList = attributesList;
		return this;
	}

	public ObjectDataSenderBuilder addAttribute(String key, Object value) {
		attributesList.put(key, value);
		return this;
	}

	public ObjectDataSenderBuilder addAttribute(WWFieldsAttributesInterface enumName, Object value) throws WWException {
		if (value.getClass().equals(enumName.getObjectClassType())) {
			attributesList.put(enumName.getLabel(), value);
		} else {
			throw new WWException("Watson Work Services expects a " + enumName.getObjectClassType().getName() + " for this attribute. Object supplied is " + value.getClass().getName());
		}
		return this;
	}

	public ObjectDataSenderBuilder removeAttribute(String key) {
		attributesList.remove(key);
		return this;
	}

	public List<DataSenderBuilder> getChildren() {
		return children;
	}

	public ObjectDataSenderBuilder setChildren(List<DataSenderBuilder> children) {
		this.children = children;
		return this;
	}

	public ObjectDataSenderBuilder addChild(DataSenderBuilder child) {
		children.add(child);
		return this;
	}

	public ObjectDataSenderBuilder removeChild(DataSenderBuilder child) {
		children.remove(child);
		return this;
	}

	public ObjectDataSenderBuilder addPageInfo() {
		pageInfo = new ObjectDataSenderBuilder(PageInfo.LABEL, PageInfo.class, false);
		return this;
	}

	public ObjectDataSenderBuilder addPageInfo(ObjectDataSenderBuilder pageInfoCustom) {
		pageInfo = pageInfoCustom;
		return this;
	}

	public ObjectDataSenderBuilder removePageInfo() {
		pageInfo = null;
		return this;
	}

}
