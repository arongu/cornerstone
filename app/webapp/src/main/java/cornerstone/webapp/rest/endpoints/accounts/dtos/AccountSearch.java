package cornerstone.webapp.rest.endpoints.accounts.dtos;

public class AccountSearch {
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
