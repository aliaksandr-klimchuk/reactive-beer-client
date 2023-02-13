package alex.klimchuk.reactive.beer.client.services;

import alex.klimchuk.reactive.beer.client.configs.WebClientConfig;
import alex.klimchuk.reactive.beer.client.model.BeerDto;
import alex.klimchuk.reactive.beer.client.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Copyright Alex Klimchuk (c) 2023.
 */
public class BeerClientServiceImplTest {

    public BeerClientService beerClient;

    @BeforeEach
    public void setUp() {
        beerClient = new BeerClientServiceImpl(new WebClientConfig().webClient());
    }

    @Test
    public void testListBeers() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent().size()).isGreaterThan(0);
    }

    @Test
    public void testListBeersPageSize10() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(1, 10, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent().size()).isEqualTo(10);
    }

    @Test
    public void testListBeersNoRecords() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(10, 20, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent().size()).isZero();
    }

    @Test
    @Disabled("API returning inventory when should not be")
    public void testGetBeerById() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();
        UUID beerId = pagedList.getContent().get(0).getId();

        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerId, false);
        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto.getId()).isEqualTo(beerId);
        assertThat(beerDto.getQuantityOnHand()).isNull();
    }

    @Test
    public void functionalTestGetBeerById() throws InterruptedException {
        AtomicReference<String> beerName = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        beerClient.listBeers(null, null, null, null, null)
                .map(beerPagedList -> beerPagedList.getContent().get(0).getId())
                .map(beerId -> beerClient.getBeerById(beerId, false))
                .flatMap(mono -> mono)
                .subscribe(beerDto -> {
                    System.out.println(beerDto.getBeerName());
                    beerName.set(beerDto.getBeerName());
                    assertThat(beerDto.getBeerName()).isEqualTo("Really Good Beer");
                    countDownLatch.countDown();
                });

        countDownLatch.await();

        assertThat(beerName.get()).isEqualTo("Really Good Beer");
    }

    @Test
    public void testGetBeerByIdShowInventoryTrue() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();
        UUID beerId = pagedList.getContent().get(0).getId();

        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerId, true);
        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto.getId()).isEqualTo(beerId);
        assertThat(beerDto.getQuantityOnHand()).isNotNull();
    }

    @Test
    public void testGetBeerByUPC() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();
        String upc = pagedList.getContent().get(0).getUpc();

        Mono<BeerDto> beerDtoMono = beerClient.getBeerByUPC(upc);
        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto.getUpc()).isEqualTo(upc);
    }

    @Test
    public void testCreateBeer() {
        BeerDto beerDto = BeerDto.builder()
                .beerName("Dogfishhead 90 Min IPA")
                .beerStyle("IPA")
                .upc("234848549559")
                .price(new BigDecimal("10.99"))
                .build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.createBeer(beerDto);
        ResponseEntity responseEntity = responseEntityMono.block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void testUpdateBeer() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();
        BeerDto beerDto = pagedList.getContent().get(0);

        BeerDto updatedBeer = BeerDto.builder()
                .beerName("Really Good Beer")
                .beerStyle(beerDto.getBeerStyle())
                .price(beerDto.getPrice())
                .upc(beerDto.getUpc())
                .build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.updateBeer(beerDto.getId(), updatedBeer);
        ResponseEntity<Void> responseEntity = responseEntityMono.block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testDeleteBeerHandleException() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());

        ResponseEntity<Void> responseEntity = responseEntityMono.onErrorResume(throwable -> {
            if (throwable instanceof WebClientResponseException) {
                WebClientResponseException exception = (WebClientResponseException) throwable;
                return Mono.just(ResponseEntity.status(exception.getStatusCode()).build());
            } else {
                throw new RuntimeException(throwable);
            }
        }).block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDeleteBeerByIdNotFound() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());

        assertThrows(WebClientResponseException.class, () -> {
            ResponseEntity<Void> responseEntity = responseEntityMono.block();

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    @Test
    public void testDeleteBeerById() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();
        BeerDto beerDto = pagedList.getContent().get(0);

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(beerDto.getId());
        ResponseEntity<Void> responseEntity = responseEntityMono.block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}
