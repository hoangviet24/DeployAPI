package product.management.electronic.dto.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequestDto {
    private List<UUID> cartItemIds;
    private String shippingAddress;
    private String note;
}
