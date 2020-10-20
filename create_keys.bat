:: censys
@ECHO OFF
set /p API_ID="Enter your Censys Api ID: "
@echo %API_ID% 1> ./censys_key
set /p SECRET="Enter your Censys Api Secret: "
@echo %SECRET% 1> ./censys_key

:: shodan
set /p API_KEY="Enter your Shodan Api Key: "
@echo %API_KEY% 1> ./shodan_key