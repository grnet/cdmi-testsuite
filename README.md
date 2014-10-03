This is a CDMI test suite, currently targeting [GRNET's CDMI implementation](https://github.com/grnet/pithos-j) but which can be generalized:

* To support any CDMI implementation
* To support other testing needs

The test suite is meant to be configurable. See [reference.conf](https://github.com/grnet/cdmi-testsuite/blob/master/src/main/resources/reference.conf)

[Currently](https://github.com/grnet/cdmi-testsuite/commit/e31137b127581d5e763976b03bf12eaaf5658a21) a successful run looks like this:

```
+Main$ConfigurationCheck$2$ [gr.grnet.cdmi.client.Main$ConfigurationCheck$2$]
  OK [`global` exists in configuration]
  OK [`global.CDMI_ROOT_URI` exists in configuration]
  OK [`global.http-headers` exists in configuration]
  OK [`global.http-headers.X-CDMI-Specification-Version` exists in configuration]
  OK [`class-tests` exists in configuration]
  OK [`shell-tests` exists in configuration]
-OK Main$ConfigurationCheck$2$ [gr.grnet.cdmi.client.Main$ConfigurationCheck$2$]
+Check availability of classes from `class-tests` [gr.grnet.cdmi.client.Main$ClassTestsCheck$2$]
  OK [class gr.grnet.cdmi.client.tests.RootCapabilityObject can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.Containers can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.DataObjects can be instantiated as a TestCase]
-OK Check availability of classes from `class-tests` [gr.grnet.cdmi.client.Main$ClassTestsCheck$2$]
+RootCapabilityObject [gr.grnet.cdmi.client.tests.RootCapabilityObject]
  OK [Get capabilities with 'X-CDMI-Specification-Version', no 'Accept']
  OK [Get capabilities with 'X-CDMI-Specification-Version' and 'Accept: application/cdmi-capability']
  OK [Get capabilities with 'X-CDMI-Specification-Version' and 'Accept: */*']
  OK [Get capabilities w/o 'X-CDMI-Specification-Version']
-OK RootCapabilityObject [gr.grnet.cdmi.client.tests.RootCapabilityObject]
+Containers [gr.grnet.cdmi.client.tests.Containers]
-OK Containers [gr.grnet.cdmi.client.tests.Containers]
+DataObjects [gr.grnet.cdmi.client.tests.DataObjects]
  OK [PUT CDMI 'Content-Type: application/cdmi-object']
  OK [GET CDMI 'Accept: application/cdmi-object' returns 'Content-Type: application/cdmi-object']
  OK [GET CDMI 'Accept: */*' returns 'Content-Type: application/cdmi-object']
  OK [GET CDMI w/o 'Accept' returns 'Content-Type: application/cdmi-object']
  OK [GET CDMI w/o 'X-Auth-Token' fails]
  OK [PUT non-CDMI 'Content-Type: text/plain']
  OK [GET non-CDMI 'Accept: */*' returns exact 'Content-Type']
  OK [GET non-CDMI w/o 'Accept' returns exact 'Content-Type']
-OK DataObjects [gr.grnet.cdmi.client.tests.DataObjects]
```