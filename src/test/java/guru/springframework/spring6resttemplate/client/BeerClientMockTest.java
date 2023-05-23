package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String URL = "http://localhost:8080";

    BeerClient beerClient;

    MockRestServiceServer server;

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerDTO dto;
    String dtoJson;

    public static final String AUTHORIZATION_VALUE = "Basic dXNlcjE6cGFzc3dvcmQxMjM0";

    @BeforeEach
    void setUp() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient =  new BeerClientImpl(mockRestTemplateBuilder);

        dto = getBeerDto();
        dtoJson = objectMapper.writeValueAsString(dto);
    }

    @Test
    void testListBeers() throws JsonProcessingException {
        String payLoad = objectMapper.writeValueAsString(getPage());

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(URL + BeerClientImpl.BEER_PATH))
                .andExpect(header("Authorization", AUTHORIZATION_VALUE))
                .andRespond(withSuccess(payLoad, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }

    @Test
    void testGetById() {
        mockGetOperation();

        BeerDTO responseDto = beerClient.getBeerById(dto.getId());
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testCreateBeer() {
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.BEER_BY_ID_PATH)
                        .build(dto.getId());

        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(URL + BeerClientImpl.BEER_PATH))
                .andExpect(header("Authorization", AUTHORIZATION_VALUE))
                .andRespond(withAccepted().location(uri));

        mockGetOperation();

        BeerDTO responseDto = beerClient.createBeer(dto);
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testUpdateBeer() {
        server.expect(method(HttpMethod.PUT))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.BEER_BY_ID_PATH, dto.getId()))
                .andExpect(header("Authorization", AUTHORIZATION_VALUE))
                .andRespond(withNoContent());

        mockGetOperation();

        BeerDTO responseDto = beerClient.updateBeer(dto);
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testDeleteBeer() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.BEER_BY_ID_PATH, dto.getId()))
                .andExpect(header("Authorization", AUTHORIZATION_VALUE))
                .andRespond(withNoContent());

        beerClient.deleteBeer(dto.getId());
        server.verify();
    }

    @Test
    void testDeleteNotFound() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.BEER_BY_ID_PATH, dto.getId()))
                .andExpect(header("Authorization", AUTHORIZATION_VALUE))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.deleteBeer(dto.getId());
        });

        server.verify();
    }

    @Test
    void testListBeersWithQueryParam() throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(getPage());

        URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.BEER_PATH)
                .queryParam("beerName", "ALE")
                .build().toUri();

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andExpect(header("Authorization", AUTHORIZATION_VALUE))
                .andExpect(queryParam("beerName", "ALE"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> responsePage = beerClient.listBeers("ALE", null, null, null, null);

        assertThat(responsePage.getContent().size()).isEqualTo(1);
    }

    private void mockGetOperation() {
        server.expect(method(HttpMethod.GET))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.BEER_BY_ID_PATH, dto.getId()))
                .andExpect(header("Authorization", AUTHORIZATION_VALUE))
                .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
    }

    BeerDTO getBeerDto() {
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.99"))
                .beerName("New beer")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("123456")
                .build();

    }

    BeerDTOPageImpl getPage() {
        return new BeerDTOPageImpl(Arrays.asList(getBeerDto()), 1, 25, 1);
    }
}
