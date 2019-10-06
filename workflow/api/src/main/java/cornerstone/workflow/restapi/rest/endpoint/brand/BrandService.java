package cornerstone.workflow.restapi.rest.endpoint.brand;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface BrandService {
    List<Map<String, String>> getBrands() throws SQLException;
}
