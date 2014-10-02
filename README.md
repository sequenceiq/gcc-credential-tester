This project aims to verify your google cloud computing credentials.

## Usage

This tool is packaged as an executable jar. So installation is just a simple
jar download.

```
curl -LO https://github.com/sequenceiq/gcc-credential-tester/releases/download/1.0/gcc-credential-tester.jar
```
###Prepeare your account

Follow the instructions: https://cloud.google.com/storage/docs/authentication#service_accounts
Create a `Service account` and `Generate a new P12 key`. Onceyou have downloaded the key use `openssl` to convert it to PEM.

`openssl pkcs12 -in path/to/key.p12 -nodes -nocerts > path/to/key.pem`

Edit the new PEM file and keep only the part between:

```
-----BEGIN RSA PRIVATE KEY-----

-----END PRIVATE KEY-----
```

Make sure that at API level (APIs and auth) you have enabled:

* Google Compute Engine
* Google Compute Engine Instance Group Manager API
* Google Compute Engine Instance Groups API

After that you are ready to use the Tester JAR.

```
java -jar gcc-credential-tester.jar <keyFile> <subscriptionId> <projectId>
```
