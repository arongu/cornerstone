package cornerstone.webapp.rest.api.accounts.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountSearch {
    @JsonProperty(required = true)
    private String searchString;

    public AccountSearch() {
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(final String searchString) {
        this.searchString = searchString;
    }
}
