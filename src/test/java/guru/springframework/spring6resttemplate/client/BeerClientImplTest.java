package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}