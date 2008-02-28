package org.electrocodeogram.cpc.store.local.xml.provider;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.UnsupportedDataTypeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingProvider;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingException;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingStore;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.store.local.xml.utils.XMLUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;


/**
 * Default {@link IMappingProvider} implementation.
 * 
 * @author vw
 */
public class XMLMappingProvider implements IMappingProvider, IManagableProvider
{
	private static final Log log = LogFactory.getLog(XMLMappingProvider.class);

	public static final String XML_MAPPING_TYPE = "cpcxml";
	public static final int XML_MAPPING_VERSION = 1;

	private IStoreProvider storeProvider;
	private ICloneFactoryProvider cloneFactoryProvider;

	public XMLMappingProvider()
	{
		log.trace("XMLMappingProvider()");

		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		assert (storeProvider != null);

		cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICloneFactoryProvider.class);
		assert (cloneFactoryProvider != null);
	}

	/*
	 * IXMLMappingProvider methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.xml.IXMLMappingProvider#isSupportedMappingFormat(java.lang.String)
	 */
	@Override
	public boolean isSupportedMappingFormat(String data)
	{
		//TODO: optimise this method.
		//Simply looking at the <cpc> header should be enough.

		try
		{
			mapFromString(data);
			return true;
		}
		catch (MappingException e)
		{
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.local.xml.provider.IXMLMappingProvider#mapToXML(org.electrocodeogram.cpc.store.local.xml.provider.XMLMappingStore, boolean)
	 */
	public String mapToString(MappingStore xmlMappingStore, boolean addPreamble)
	{
		if (log.isTraceEnabled())
			log.trace("mapToXML() - xmlMappingStore: " + xmlMappingStore + ", addPreamble: " + addPreamble);

		//initialise string builder
		StringBuilder sb = new StringBuilder();

		//Add preamble, if requested.
		if (addPreamble)
		{
			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<cpc type=\"");
			sb.append(XML_MAPPING_TYPE);
			sb.append("\" version=\"");
			sb.append(XML_MAPPING_VERSION);
			sb.append("\">\n");
		}
		//TODO: add schema information here?

		//add actual data
		_mapToXML(xmlMappingStore.getStatefulParentObject(), xmlMappingStore.getStatefulChildObjects(), sb);

		if (addPreamble)
		{
			sb.append("</cpc>\n\n");
		}

		//Ok, we're done.
		String result = sb.toString();

		if (log.isTraceEnabled())
			log.trace("mapToXML() - result: " + result);

		return result;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.local.xml.provider.IXMLMappingProvider#mapFromXML(java.lang.String)
	 */
	public MappingStore mapFromString(String xml) throws MappingException
	{
		if (log.isTraceEnabled())
			log.trace("mapFromXML() - xml: " + xml);

		try
		{
			IncrementalXMLMappingStore result = new IncrementalXMLMappingStore();

			//parse the xml data
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new StringReader(xml));

			//make sure we understand the given format (throws UnsupportedDataTypeException)
			ensureVersionCompatibility(doc);

			//get main object
			Element object = doc.getRootElement().getChild("object");
			parseObject(object, true, result);

			if (log.isTraceEnabled())
				log.trace("mapFromXML() - result: " + result);

			return result;
		}
		catch (Exception e)
		{
			log.error("mapFromXML() - error while parsing xml data - " + e, e);
			log.info("mapFromXML() - corrupt xml data: " + CoreStringUtils.quoteString(xml));
			throw new MappingException("Error while mapping xml data - " + e, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.xml.IXMLMappingProvider#extractCloneObjectUuidFromXML(java.lang.String)
	 */
	@Override
	public String extractCloneObjectUuidFromString(String xml) throws MappingException
	{
		//TODO: we're only interested in the main object's UUID, this could be optimised!

		MappingStore mappingStore = mapFromString(xml);
		IStatefulObject parentObject = mappingStore.getStatefulParentObject();
		if (parentObject == null || (!(parentObject instanceof ICloneObject)))
			return null;

		return ((ICloneObject) parentObject).getUuid();
	}

	/**
	 * Checks whether the given XML document as a header which indicates that this xml mapping
	 * provider can understand the document.
	 * 
	 * @param doc the document to check, never null.
	 * 
	 * @throws UnsupportedDataTypeException thrown if the given xml document is not in a supported format. 
	 */
	private void ensureVersionCompatibility(Document doc) throws UnsupportedDataTypeException
	{
		if (!doc.getRootElement().getName().equals("cpc"))
			throw new UnsupportedDataTypeException("Unsupported CPC XML Format - invalid root element");

		if (!XML_MAPPING_TYPE.equals(doc.getRootElement().getAttributeValue("type")))
			throw new UnsupportedDataTypeException("Unsupported CPC XML Format - invalid cpc format type");

		if (!Integer.toString(XML_MAPPING_VERSION).equals(doc.getRootElement().getAttributeValue("version")))
			throw new UnsupportedDataTypeException("Unsupported CPC XML Format - invalid cpc format version");
	}

	/**
	 * 
	 * @param objectElement
	 * @param isParentElement
	 * @param result
	 * 
	 * @throws UnsupportedDataTypeException 
	 */
	private IStatefulObject parseObject(Element objectElement, boolean isParentObject, IncrementalXMLMappingStore result)
			throws UnsupportedDataTypeException
	{
		if (log.isTraceEnabled())
			log.trace("parseObject() - objectElement: " + objectElement + ", isParentObject: " + isParentObject
					+ ", result: " + result);

		/*
		 * Some integrity checking.
		 */
		if (objectElement == null)
			throw new UnsupportedDataTypeException("Unsupported CPC XML Format - object element not found");

		/*
		 * First find out what kind of object this is
		 * and get a new instance for it.
		 */
		IStatefulObject statefulObject = createInstance(objectElement);

		/*
		 * Now fill the instance with data.
		 */
		Map<String, Comparable<? extends Object>> stateMap = parseState(objectElement, statefulObject.getStateTypes());
		if (stateMap != null)
			statefulObject.setState(stateMap);

		/*
		 * Check for extensions
		 */
		if (statefulObject instanceof ICloneObject)
			parseExtensions(objectElement, (ICloneObject) statefulObject);

		/*
		 * Check for children.
		 */
		if (isParentObject)
		{
			parseChildren(objectElement, result);

			result.setStatefulParentObject(statefulObject);
		}

		return statefulObject;
	}

	@SuppressWarnings("unchecked")
	private void parseChildren(Element objectElement, IncrementalXMLMappingStore result)
			throws UnsupportedDataTypeException
	{
		if (log.isTraceEnabled())
			log.trace("parseChildren() - objectElement: " + objectElement + ", result: " + result);

		Element childrenElement = objectElement.getChild("children");
		if (childrenElement != null)
		{
			List<Element> childElements = childrenElement.getChildren("object");
			if (childElements != null && !childElements.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("parseObject() - object has " + childElements.size() + " children objects.");

				for (Element childElement : childElements)
				{
					IStatefulObject childStatefulObject = parseObject(childElement, false, result);

					result.addStatefulChildObject(childStatefulObject);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void parseExtensions(Element objectElement, ICloneObject cloneObject) throws UnsupportedDataTypeException
	{
		if (log.isTraceEnabled())
			log.trace("parseExtensions() - objectElement: " + objectElement + ", cloneObject: " + cloneObject);

		Element extensionsElement = objectElement.getChild("extensions");

		if (extensionsElement != null)
		{
			List<Element> extensionElements = extensionsElement.getChildren("object");
			if (extensionElements != null && !extensionElements.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("parseObject() - object has " + extensionElements.size() + " extension objects.");

				for (Element extensionElement : extensionElements)
				{
					//get instance
					IStatefulObject extensionStatefulObject = createInstance(extensionElement);
					assert (extensionStatefulObject instanceof ICloneObjectExtension);

					//state
					Map<String, Comparable<? extends Object>> stateMap = parseState(extensionElement,
							extensionStatefulObject.getStateTypes());
					if (stateMap != null)
						extensionStatefulObject.setState(stateMap);

					//subelements
					parseSubElements(extensionElement, extensionStatefulObject);

					cloneObject.addExtension((ICloneObjectExtension) extensionStatefulObject);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void parseSubElements(Element extensionElement, IStatefulObject extensionStatefulObject)
			throws UnsupportedDataTypeException
	{
		if (log.isTraceEnabled())
			log.trace("parseSubElements() - extensionElement: " + extensionElement + ", extensionStatefulObject: "
					+ extensionStatefulObject);

		Element subelementsElement = extensionElement.getChild("subelements");

		if (subelementsElement != null)
		{
			assert (extensionStatefulObject instanceof ICloneObjectExtensionMultiStatefulObject);
			ICloneObjectExtensionMultiStatefulObject multiStatefulObject = (ICloneObjectExtensionMultiStatefulObject) extensionStatefulObject;

			List<Element> subelementElements = subelementsElement.getChildren("subelement");
			if (subelementElements != null && !subelementElements.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("parseObject() - extension has " + subelementElements.size() + " subelement objects.");

				List<List<Map<String, Comparable<? extends Object>>>> multiStateMaps = new LinkedList<List<Map<String, Comparable<? extends Object>>>>();

				int pos = 0;
				for (Element subelementElement : subelementElements)
				{
					List<Map<String, Comparable<? extends Object>>> stateMaps = parseSubElement(subelementElement,
							multiStatefulObject, pos);
					multiStateMaps.add(stateMaps);
					++pos;
				}

				multiStatefulObject.setMultiState(multiStateMaps);
			}
		}
	}

	/*
	 * <subelement type="clone_diff"> <state/>* </subelement>
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Comparable<? extends Object>>> parseSubElement(Element subElementElement,
			ICloneObjectExtensionMultiStatefulObject multiStatefulObject, int pos) throws UnsupportedDataTypeException
	{
		if (log.isTraceEnabled())
			log.trace("parseSubElement() - subElementElement: " + subElementElement + ", multiStatefulObject: "
					+ multiStatefulObject + ", pos: " + pos);

		List<Element> stateElements = subElementElement.getChildren("state");
		if (stateElements != null && !stateElements.isEmpty())
		{
			List<Map<String, Comparable<? extends Object>>> stateMaps = new ArrayList<Map<String, Comparable<? extends Object>>>(
					stateElements.size());

			if (log.isTraceEnabled())
				log.trace("parseObject() - subelement has " + stateElements.size() + " state elements.");

			Map<String, Class<? extends Object>> stateTypes = multiStatefulObject.getMultiStateTypes().get(pos);

			for (Element stateElement : stateElements)
			{
				Map<String, Comparable<? extends Object>> stateMap = parseState(stateElement, stateTypes);
				stateMaps.add(stateMap);
			}

			return stateMaps;
		}

		return new ArrayList<Map<String, Comparable<? extends Object>>>(0);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Comparable<? extends Object>> parseState(Element objectElement,
			Map<String, Class<? extends Object>> stateTypes) throws UnsupportedDataTypeException
	{
		if (log.isTraceEnabled())
			log.trace("parseState() - objectElement: " + objectElement + ", stateTypes: " + stateTypes);

		Element stateElement;
		if (objectElement.getName().equals("state"))
			stateElement = objectElement;
		else
			stateElement = objectElement.getChild("state");

		if (stateElement == null)
			throw new UnsupportedDataTypeException("Unsupported CPC XML Format - state element missing: "
					+ objectElement);

		List<Element> attrElements = stateElement.getChildren("attr");
		if (attrElements != null && !attrElements.isEmpty())
		{
			//build state map
			Map<String, Comparable<? extends Object>> stateMap = new HashMap<String, Comparable<? extends Object>>(
					attrElements.size());
			for (Element attrElement : attrElements)
			{
				String key = attrElement.getAttributeValue("name");
				String value = attrElement.getText();

				if (key == null || value == null)
					throw new UnsupportedDataTypeException("Unsupported CPC XML Format - attr element invalid: "
							+ attrElement);

				//FIXME: / TODO: do we need to convert the types here?
				String plainKey = XMLUtils.unEscapeIdentifier(key);
				stateMap.put(plainKey, XMLUtils.convertValueToType(stateTypes.get(plainKey), XMLUtils
						.unEscapeData(value)));
			}

			if (log.isTraceEnabled())
				log.trace("parseObject() - extracted state map: " + stateMap);

			return stateMap;
		}

		return null;
	}

	private IStatefulObject createInstance(Element objectElement) throws UnsupportedDataTypeException
	{
		if (log.isTraceEnabled())
			log.trace("createInstance() - objectElement: " + objectElement);

		String type = objectElement.getAttributeValue("type");
		if (type == null)
			throw new UnsupportedDataTypeException("Unsupported CPC XML Format - object element type not specified");

		IStatefulObject statefulObject = cloneFactoryProvider.getInstanceByPersistenceClassIdentifier(type);
		if (statefulObject == null)
			throw new UnsupportedDataTypeException("Unsupported CPC XML Format - object element type not supported: "
					+ type);

		if (log.isTraceEnabled())
			log.trace("parseObject() - created new stateful object instance: " + statefulObject);

		return statefulObject;
	}

	/*
	 * IProvider methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Store Local XML - XML Mapping Provider";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
		//nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
		//nothing to do
	}

	/*
	 * Private methods.
	 */

	private void _mapToXML(IStatefulObject statefulParentObject, List<IStatefulObject> statefulChildObjects,
			StringBuilder sb)
	{
		if (statefulParentObject != null)
		{
			_mapToXML(statefulParentObject, false, sb);
			sb.append("<children>\n");
		}

		for (IStatefulObject statefulChildObject : statefulChildObjects)
		{
			_mapToXML(statefulChildObject, true, sb);
		}

		if (statefulParentObject != null)
		{
			sb.append("</children>\n");
			_closeElement(statefulParentObject, sb);
		}
	}

	private void _mapToXML(IStatefulObject statefulObject, boolean closeElement, StringBuilder sb)
	{
		if (log.isTraceEnabled())
			log.trace("_mapToXML() - statefulObject: " + statefulObject + ", closeElement: " + closeElement);

		String objectElementName = XMLUtils.escapeIdentifier(statefulObject.getPersistenceClassIdentifier());

		if (log.isTraceEnabled())
			log.trace("_mapToXML() - mapping object as type: " + objectElementName);

		//start a new element for this object
		sb.append("<object type=\"");
		sb.append(objectElementName);
		sb.append("\">\n");

		/*
		 * Now add the element state.
		 */
		Map<String, Comparable<? extends Object>> state = statefulObject.getState();
		_mapState(state, sb);

		/*
		 * Check if we need to map any extension data.
		 */
		if (statefulObject instanceof ICloneObject)
		{
			log.trace("_mapToXML() - object is ICloneObject, mapping extensions.");

			sb.append("<extensions>\n");
			ICloneObject statefulCloneObject = (ICloneObject) statefulObject;

			if (statefulCloneObject.hasExtensions())
			{
				for (ICloneObjectExtension extension : statefulCloneObject.getExtensions())
				{
					if (extension instanceof ICloneObjectExtensionStatefulObject)
					{
						//Ok, we'll need to persist this extension.

						//check if this is a lazy loaded extension
						if (extension instanceof ICloneObjectExtensionLazyMultiStatefulObject)
						{
							//make sure it is fully loaded
							if (extension.isPartial())
								//load all sub element data
								extension = storeProvider.getFullCloneObjectExtension(statefulCloneObject,
										(ICloneObjectExtensionLazyMultiStatefulObject) extension);
						}

						//now map the extension like a any other stateful object
						_mapToXML((ICloneObjectExtensionStatefulObject) extension, true, sb);
					}
				}
			}

			sb.append("</extensions>\n");
		}
		/*
		 * Check if this might be a multi stateful extension. 
		 */
		else if (statefulObject instanceof ICloneObjectExtensionMultiStatefulObject)
		{
			log.trace("_mapToXML() - object is ICloneObjectExtensionMultiStatefulObject, mapping sub-elements.");

			sb.append("<subelements>\n");

			List<String> multiIdentifiers = ((ICloneObjectExtensionMultiStatefulObject) statefulObject)
					.getMultiPersistenceClassIdentifier();
			List<List<Map<String, Comparable<? extends Object>>>> multiStates = ((ICloneObjectExtensionMultiStatefulObject) statefulObject)
					.getMultiState();

			int i = 0;
			for (String identifier : multiIdentifiers)
			{
				sb.append("<subelement type=\"");
				sb.append(XMLUtils.escapeIdentifier(identifier));
				sb.append("\">\n");

				for (Map<String, Comparable<? extends Object>> subState : multiStates.get(i))
				{
					_mapState(subState, sb);
				}

				sb.append("</subelement>\n");
				++i;
			}

			sb.append("</subelements>\n");
		}

		//we're done, close our element
		if (closeElement)
		{
			sb.append("</object>\n\n");
		}
	}

	private void _mapState(Map<String, Comparable<? extends Object>> state, StringBuilder sb)
	{
		sb.append("<state>\n");

		//make sure the order of the elements is always the same
		//otherwise the diffs between revisions of the xml files will get unnecessarily big.
		List<Map.Entry<String, Comparable<? extends Object>>> entryList = new ArrayList<Map.Entry<String, Comparable<? extends Object>>>(
				state.entrySet());
		Collections.sort(entryList, new Comparator<Map.Entry<String, Comparable<? extends Object>>>()
		{
			@Override
			public int compare(Entry<String, Comparable<? extends Object>> o1,
					Entry<String, Comparable<? extends Object>> o2)
			{
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		for (Map.Entry<String, Comparable<? extends Object>> entry : entryList)
		{
			//don't print null fields (allows distinguishing null and "" fields on restore)
			if (entry.getValue() == null)
				continue;

			sb.append("<attr name=\"");
			sb.append(XMLUtils.escapeIdentifier(entry.getKey()));
			sb.append("\">");
			sb.append(XMLUtils.mapToXMLValue(entry.getValue()));
			sb.append("</attr>\n");
		}

		sb.append("</state>\n");
	}

	private void _closeElement(IStatefulObject statefulObject, StringBuilder sb)
	{
		sb.append("</object>\n\n");
	}

}
