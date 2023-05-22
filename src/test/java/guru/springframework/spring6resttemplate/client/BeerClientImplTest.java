package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClient beerClient;

    @Test
    void testListBeers() {
        beerClient.listBeers();
    }

    @Test
    void testListBeersPageNumber() {
        beerClient.listBeers(null, null, null, 2, null);
    }

    @Test
    void testListBeersByBeerName() {
        beerClient.listBeers("ALE", null, null, null, null);
    }

    @Test
    void testListBeersByBeerNameAndStyle() {
        beerClient.listBeers("ALE", BeerStyle.PALE_ALE, null, null, null);
    }

    @Test
    void testListBeersShowInventory() {
        beerClient.listBeers("ALE", null, true, null, null);
    }

    @Test
    void testGetBeerById() {
        Page<BeerDTO> beerDTOS = beerClient.listBeers();

        BeerDTO dto = beerDTOS.getContent().get(0);

        BeerDTO byId = beerClient.getBeerById(dto.getId());

        assertNotNull(byId);
    }

    @Test
    void testCreateBeer() {
        BeerDTO newDto = BeerDTO.builder()
                .beerName("New beer")
                .beerStyle(BeerStyle.LAGER)
                .price(new BigDecimal("9.99"))
                .quantityOnHand(100)
                .upc("1234")
                .build();

        BeerDTO savedDto = beerClient.createBeer(newDto);

        assertNotNull(savedDto);
    }

    @Test
    void testUpdateBeer() {
        BeerDTO newDto = BeerDTO.builder()
                .beerName("New beer")
                .beerStyle(BeerStyle.LAGER)
                .price(new BigDecimal("9.99"))
                .quantityOnHand(100)
                .upc("1234")
                .build();

        BeerDTO beerDto = beerClient.createBeer(newDto);

        final String newName = "New beer 2";
        beerDto.setBeerName(newName);
        BeerDTO updatedBeer = beerClient.updateBeer(beerDto);

        assertEquals(updatedBeer.getBeerName(), newName);
    }

    @Test
    void testDeleteBeer() {
        BeerDTO newDto = BeerDTO.builder()
                .beerName("New beer")
                .beerStyle(BeerStyle.LAGER)
                .price(new BigDecimal("9.99"))
                .quantityOnHand(100)
                .upc("1234")
                .build();

        BeerDTO beerDto = beerClient.createBeer(newDto);

        beerClient.deleteBeer(beerDto.getId());

        assertThrows(HttpClientErrorException.class, () -> {
            //should error
            beerClient.getBeerById(beerDto.getId());
        });
    }
}