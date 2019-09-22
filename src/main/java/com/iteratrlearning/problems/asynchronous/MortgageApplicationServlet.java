package com.iteratrlearning.problems.asynchronous;

import com.iteratrlearning.examples.asynchronous.bank.AsyncAccountProxy;
import com.iteratrlearning.examples.asynchronous.bank.AsyncCreditCheckProxy;
import com.iteratrlearning.examples.asynchronous.bank.AsyncCustomerEndPoint;
import com.iteratrlearning.examples.synchronous.account.BalanceReport;
import com.iteratrlearning.examples.synchronous.credit_check.CreditReport;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import static com.iteratrlearning.answers.asynchronous.MortgageApplicationService.AMOUNT_TO_BORROW;
import static javax.servlet.http.HttpServletResponse.*;

public class MortgageApplicationServlet extends AsyncCustomerEndPoint {

    private final AsyncAccountProxy accountProxy = new AsyncAccountProxy(client, objectMapper);
    private final AsyncCreditCheckProxy creditProxy = new AsyncCreditCheckProxy(client, objectMapper);

    @Override
    protected void doGetCustomer(final String customerId, final AsyncContext context) throws Exception {
        final int amountToBorrow = Integer.parseInt(context.getRequest().getParameter(AMOUNT_TO_BORROW));
        final MortgageHandler handler = new MortgageHandler(context, amountToBorrow);

        accountProxy.getBalance(customerId,
                handler::onBalanceReport,
                onError(context));

        creditProxy.getCreditReport(customerId,
                handler::onCreditReport);
    }

    private class MortgageHandler {

        final AsyncContext context;
        final int amountToBorrow;
        BalanceReport balanceReport;
        CreditReport creditReport;

        private MortgageHandler(AsyncContext context, int amountToBorrow) {
            this.context = context;
            this.amountToBorrow = amountToBorrow;
        }

        private void onBalanceReport(BalanceReport balanceReport) {
            this.balanceReport = balanceReport;
            processMortgageRequest();
        }

        private void onCreditReport(CreditReport creditReport) {
            this.creditReport = creditReport;
            processMortgageRequest();
        }

        private void processMortgageRequest() {
            if (balanceReport != null && creditReport != null) {
                if (canGrantMortgage()) {
                    grantMortgage();
                } else {
                    denyMortgage();
                }
            }
        }

        private boolean canGrantMortgage() {
            int balance = balanceReport.getBalance();
            int creditScore = creditReport.getCreditScore();

            return amountToBorrow <= 4 * balance && creditScore > 700;
        }

        private void grantMortgage() {
            returnResponse(SC_OK);
        }

        private void denyMortgage() {
            returnResponse(SC_FORBIDDEN);
        }

        private void returnResponse(int requestStatus) {
            final HttpServletResponse response = (HttpServletResponse) context.getResponse();
            response.setStatus(requestStatus);
            context.complete();
        }
    }

}
