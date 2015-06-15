<%@ tag body-content="scriptless" trimDirectiveWhitespaces="true" %>
<%@ attribute name="maxPages" required="true" %>
<%@ attribute name="maxElements" required="true" %>
<%@ attribute name="page" required="true" %>
<%@ attribute name="size" required="true" %>

<div class="pagination">
    Page ${page + 1} of ${maxPages}. Total ${maxElements} Entries.
</div>