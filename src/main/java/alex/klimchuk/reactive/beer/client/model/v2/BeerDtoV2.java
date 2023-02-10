package alex.klimchuk.reactive.beer.client.model.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Copyright Alex Klimchuk (c) 2023.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeerDtoV2 {

    private UUID id;

    private String beerName;

    private BeerStyleEnum beerStyle;

    private String upc;

    private BigDecimal price;

    private Integer quantityOnHand;

}
