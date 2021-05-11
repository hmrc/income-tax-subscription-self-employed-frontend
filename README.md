
# income-tax-subscription-self-employed-frontend

This is a placeholder README.md for a new repository

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


1) **Start the service:**

   `sbt 'run 9563 -Dapplication.routes=testOnlyDoNotUseInAppConf.Routes'`

    NB: The capitalisation of 'routes' is important.

2) You'll need to have the auth stub running from the Service Manager (`http://localhost:9949/auth-login-stub/gg-sign-in`). 
   
   Set the callback url to 

   `http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/details`

   or

   `http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/client/details` 
   
   for an agent view

3) Access the landing page via the auth stub

