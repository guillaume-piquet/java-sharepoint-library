package org.korecky.sharepoint;

import org.korecky.sharepoint.support.WsContext;
import org.korecky.sharepoint.net.HttpProxy;
import org.korecky.sharepoint.authentication.AbstractAuthenticator;
import com.microsoft.schemas.sharepoint.soap.webs.GetAllSubWebCollectionResponse.GetAllSubWebCollectionResult;
import com.microsoft.schemas.sharepoint.soap.webs.GetWebResponse.GetWebResult;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a collection of sites in a Web application, including a top-level
 * Web site and all its subsites. Each SPSite object, or site collection, is
 * represented within an SPSiteCollection object that consists of the collection
 * of all site collections in the Web application.
 *
 * @author Vladislav Korecký [vladislav@korecky.org] - http://www.korecky.org
 *
 */
public class SPSite {

    private URL url;

    /**
     * Initializes a new instance of the SPSite
     *
     * @param url
     * @param authenticator
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public SPSite(URL url, AbstractAuthenticator authenticator) throws NoSuchAlgorithmException, KeyManagementException {
        this(url, authenticator, null, false);
    }

    /**
     * Initializes a new instance of the SPSite
     *
     * @param url
     * @param authenticator
     * @param trustAllSSLs
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public SPSite(URL url, AbstractAuthenticator authenticator, boolean trustAllSSLs) throws NoSuchAlgorithmException, KeyManagementException {
        this(url, authenticator, null, trustAllSSLs);
    }

    /**
     * Initializes a new instance of the SPSite
     *
     * @param url
     * @param authenticator
     * @param httpProxy
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public SPSite(URL url, AbstractAuthenticator authenticator, HttpProxy httpProxy) throws NoSuchAlgorithmException, KeyManagementException {
        this(url, authenticator, httpProxy, false);
    }

    /**
     * Initializes a new instance of the SPSite
     *
     * @param url
     * @param authenticator
     * @param httpProxy
     * @param trustAllSSLs
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public SPSite(URL url, AbstractAuthenticator authenticator, HttpProxy httpProxy, boolean trustAllSSLs) throws NoSuchAlgorithmException, KeyManagementException {
        this.url = url;
        WsContext.setSiteUrl(url);
        WsContext.setAuthenticator(authenticator);
        WsContext.setHttpProxy(httpProxy);
        WsContext.setTrustAllSSLs(trustAllSSLs);
        WsContext.configureEnviroment();
    }

    /**
     * Gets the root Web site of the site collection.
     *
     * @return NodeList contains web elements
     */
    public SPWeb getRootWeb() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
        SPWeb rootWeb = null;
        String rootWebUrl = StringUtils.removeEndIgnoreCase(url.toString(), "/");
        GetWebResult result = WsContext.getWebsPort(url).getWeb(rootWebUrl);
        if (result.getContent() != null) {
            for (Object content : result.getContent()) {
                if (content instanceof Element) {
                    // Parse XML file                                       
                    Element webElement = (Element) content;
                    if (StringUtils.equals(webElement.getLocalName(), "Web")) {
                        rootWeb = new SPWeb();
                        rootWeb.loadFromXml(webElement);
                    }
                }
            }
        }
        return rootWeb;
    }

    /**
     * Gets the collection of all Web sites that are contained within the site
     * collection, including the top-level site and its subsites.
     *
     * @return NodeList contains web elements
     */
    public List<SPWeb> getAllWebs() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
        List<SPWeb> allWebs = null;
        GetAllSubWebCollectionResult result = WsContext.getWebsPort(url).getAllSubWebCollection();

        if (result.getContent() != null) {
            for (Object content : result.getContent()) {
                if (content instanceof Element) {
                    // Parse XML file                    
                    Element rootElement = (Element) content;
                    if (StringUtils.equals(rootElement.getLocalName(), "Webs")) {
                        allWebs = new ArrayList<SPWeb>();
                        NodeList webNodeList = rootElement.getElementsByTagName("Web");
                        for (int i = 0; i < webNodeList.getLength(); i++) {
                            Element webElement = (Element) webNodeList.item(i);
                            SPWeb web = new SPWeb();
                            web.loadFromXml(webElement);
                            allWebs.add(web);
                        }
                    }
                }
            }
        }
        return allWebs;
    }
}
