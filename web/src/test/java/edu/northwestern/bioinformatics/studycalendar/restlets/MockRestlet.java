package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * @author Jalpa Patel
*/
final class MockRestlet extends Restlet {
    private Request lastRequest;
    private Response lastResponse;
    private RuntimeException toThrow;

    @Override
    public void handle(Request request, Response response) {
        this.lastRequest = request;
        this.lastResponse = response;
        if (toThrow != null) {
            throw toThrow;
        }
    }

    public boolean handleCalled() {
        return lastRequest != null;
    }

    public void setException(RuntimeException toThrow) {
        this.toThrow = toThrow;
    }
}
