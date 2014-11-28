## Intro

This is a CDMI test suite, currently targeting [GRNET's CDMI implementation](https://github.com/grnet/pithos-j) but which can be generalized:

* To support any CDMI implementation
* To support other testing needs

The test suite is meant to be configurable. See [reference.conf](https://github.com/grnet/cdmi-testsuite/blob/master/src/main/resources/reference.conf)

A successful run looks like this:

```
+Master configuration exists [gr.grnet.cdmi.client.Main$MasterConfCheck$2$]
  OK [Check provided configuration]
-OK Master configuration exists [gr.grnet.cdmi.client.Main$MasterConfCheck$2$]
+Check availability of classes from `tests` [gr.grnet.cdmi.client.Main$ClassTestsCheck$2$]
  OK [class gr.grnet.cdmi.client.tests.ServerConnection can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.RootCapabilityObject can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.DataObjectsCDMI can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.DataObjectsNonCDMI can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.ContainersCDMI_I can be instantiated as a TestCase]
-OK Check availability of classes from `tests` [gr.grnet.cdmi.client.Main$ClassTestsCheck$2$]
+ServerConnection [gr.grnet.cdmi.client.tests.ServerConnection]
  OK [Ping Server]
-OK ServerConnection [gr.grnet.cdmi.client.tests.ServerConnection]
+RootCapabilityObject [gr.grnet.cdmi.client.tests.RootCapabilityObject]
  OK [Get capabilities with 'X-CDMI-Specification-Version', no 'Accept']
  OK [Get capabilities with 'X-CDMI-Specification-Version' and 'Accept: application/cdmi-capability']
  OK [Get capabilities with 'X-CDMI-Specification-Version' and 'Accept: */*']
  OK [Get capabilities w/o 'X-CDMI-Specification-Version']
-OK RootCapabilityObject [gr.grnet.cdmi.client.tests.RootCapabilityObject]
+DataObjectsCDMI [gr.grnet.cdmi.client.tests.DataObjectsCDMI]
  OK [PUT CDMI 'Content-Type: application/cdmi-object']
  OK [GET CDMI 'Accept: application/cdmi-object' returns 'Content-Type: application/cdmi-object']
  OK [GET CDMI 'Accept: */*' returns 'Content-Type: application/cdmi-object']
  OK [GET CDMI w/o 'Accept' returns 'Content-Type: application/cdmi-object']
  OK [DELETE CDMI]
-OK DataObjectsCDMI [gr.grnet.cdmi.client.tests.DataObjectsCDMI]
+DataObjectsNonCDMI [gr.grnet.cdmi.client.tests.DataObjectsNonCDMI]
  OK [PUT non-CDMI 'Content-Type: text/plain']
  OK [GET non-CDMI 'Accept: */*' returns exact 'Content-Type']
  OK [GET non-CDMI w/o 'Accept' returns exact 'Content-Type']
  OK [DELETE non-CDMI]
-OK DataObjectsNonCDMI [gr.grnet.cdmi.client.tests.DataObjectsNonCDMI]
+ContainersCDMI_I [gr.grnet.cdmi.client.tests.ContainersCDMI_I]
  OK [PUT CDMI w/ 'Content-Type: application/cdmi-container']
  OK [GET CDMI w/ 'Accept: application/cdmi-container']
  OK [GET CDMI w/o 'Accept']
  OK [GET CDMI w/ 'Accept: */*']
  OK [DELETE CDMI]
-OK ContainersCDMI_I [gr.grnet.cdmi.client.tests.ContainersCDMI_I]
```

## Usage

```
Usage: gr.grnet.cdmi.client.Main [options]
  Options:
    -h, -help, --help

       Default: false
  * -c
       The configuration file the application uses. Use 'default' to load the
       builtin configuration, though it may not be of much help for the target CDMI
       server.
```
