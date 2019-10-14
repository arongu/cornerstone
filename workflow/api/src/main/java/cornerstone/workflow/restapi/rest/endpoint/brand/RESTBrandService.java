package cornerstone.workflow.restapi.rest.endpoint.brand;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Path("/brands")
public class RESTBrandService {
    private BrandService brandService;

    @Inject
    public RESTBrandService(final BrandService brandService) {
        this.brandService = brandService;
    }

    @GET
    public List<Map<String,String>> getBrands() throws SQLException {
        return brandService.getBrands();
    }
}
