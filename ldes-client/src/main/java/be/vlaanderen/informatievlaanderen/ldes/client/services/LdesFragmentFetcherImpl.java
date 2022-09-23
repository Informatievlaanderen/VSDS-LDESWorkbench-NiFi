package be.vlaanderen.informatievlaanderen.ldes.client.services;

import static java.util.Arrays.stream;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.vlaanderen.informatievlaanderen.ldes.client.exception.UnparseableFragmentException;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;

public class LdesFragmentFetcherImpl implements LdesFragmentFetcher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LdesFragmentFetcherImpl.class);

	public static final String CACHE_CONTROL = "cache-control";
	public static final String IMMUTABLE = "immutable";
	public static final String MAX_AGE = "max-age";

	private final Lang dataSourceFormat;

	public LdesFragmentFetcherImpl(Lang lang) {
		this.dataSourceFormat = lang;
	}
	
	@Override
	public Lang getDataSourceFormat() {
		return dataSourceFormat;
	}

	@Override
	public LdesFragment fetchFragment(String fragmentUrl) {
        LdesFragment fragment = new LdesFragment();

        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpClientContext context = HttpClientContext.create();

            HttpResponse httpResponse = httpClient.execute(new HttpGet(fragmentUrl), context);
            
            fragment.setFragmentId(Optional.ofNullable(context.getRedirectLocations())
                    .flatMap(uris -> uris.stream().reduce((uri, uri2) -> uri2))
                    .map(URI::toString)
                    .orElse(fragmentUrl));

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                RDFParser.source(httpResponse.getEntity().getContent())
                        .forceLang(dataSourceFormat)
                        .parse(fragment.getModel());
                
                stream(httpResponse.getHeaders(CACHE_CONTROL))
                        .findFirst()
                        .ifPresent(header -> {
                            if (stream(header.getElements()).noneMatch(headerElement -> IMMUTABLE.equals(headerElement.getName()))) {
                                Long pollingInterval = stream(header.getElements())
                                        .filter(headerElement -> MAX_AGE.equals(headerElement.getName()))
                                        .findFirst()
                                        .map(HeaderElement::getValue)
                                        .map(Long::parseLong)
                                        .orElse(null);
                                fragment.setExpirationDate(LocalDateTime.now().plusSeconds(pollingInterval));
                            }
                            else {
                            	fragment.setImmutable(true);
                            }
                        });
            }
        } catch (IOException e) {
            LOGGER.error("An I/O exception occurred while fetching fragment {}", fragmentUrl, e);
        } catch (RiotException e){
            throw new UnparseableFragmentException(fragmentUrl, e);
        }
        
        return fragment;
    }
}
