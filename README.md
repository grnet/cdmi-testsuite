## Intro

This is a CDMI test suite. The initial use-case was testing [GRNET's CDMI implementation](https://github.com/grnet/snf-cdmi),
which in turn is based on a generic [CDMI skeleton server](https://github.com/grnet/cdmi-spec).

The test suite is [configuration-based](https://github.com/grnet/cdmi-testsuite/blob/master/src/main/resources/reference.conf)
and can be extended to support:

* Any CDMI implementation
* Other testing needs

For example, the configuration to run against GRNET's Pithos backend storage service is
[straightforward to define](https://github.com/grnet/cdmi-testsuite/blob/master/src/main/resources/pithosj-reference.conf) by manipulating the relevant HTTP headers.

A successful run looks like this:

```
+Master configuration exists [gr.grnet.cdmi.client.Main$MasterConfCheck$2$]
  OK [Check provided configuration]
-OK Master configuration exists [gr.grnet.cdmi.client.Main$MasterConfCheck$2$]
+Check availability of classes from `tests` [gr.grnet.cdmi.client.Main$ClassTestsCheck$2$]
  OK [class gr.grnet.cdmi.client.tests.ServerConnection can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.RootCapabilityObject can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.ContainersCDMI_I can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.ContainersCDMI_II can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.ContainersCDMI_III can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.ContainersCDMI_IV can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.DataObjectsCDMI can be instantiated as a TestCase]
  OK [class gr.grnet.cdmi.client.tests.DataObjectsNonCDMI can be instantiated as a TestCase]
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
+ContainersCDMI_I [gr.grnet.cdmi.client.tests.ContainersCDMI_I]
  OK [PUT CDMI w/ 'Content-Type: application/cdmi-container']
  OK [GET CDMI w/ 'Accept: application/cdmi-container']
  OK [GET CDMI w/o 'Accept']
  OK [GET CDMI w/ 'Accept: */*']
  OK [DELETE CDMI]
-OK ContainersCDMI_I [gr.grnet.cdmi.client.tests.ContainersCDMI_I]
+ContainersCDMI_II [gr.grnet.cdmi.client.tests.ContainersCDMI_II]
  OK [PUT CDMI w/ 'Content-Type: application/cdmi-container']
  OK [GET CDMI w/ 'Accept: application/cdmi-container']
  OK [GET CDMI w/o 'Accept']
  OK [GET CDMI w/ 'Accept: */*']
  OK [DELETE CDMI]
-OK ContainersCDMI_II [gr.grnet.cdmi.client.tests.ContainersCDMI_II]
+ContainersCDMI_III [gr.grnet.cdmi.client.tests.ContainersCDMI_III]
  OK [PUT CDMI w/ 'Content-Type: application/cdmi-container' and 'Accept: application/cdmi-container']
  OK [GET CDMI w/ 'Accept: application/cdmi-container']
  OK [GET CDMI w/o 'Accept']
  OK [GET CDMI w/ 'Accept: */*']
  OK [DELETE CDMI]
-OK ContainersCDMI_III [gr.grnet.cdmi.client.tests.ContainersCDMI_III]
+ContainersCDMI_IV [gr.grnet.cdmi.client.tests.ContainersCDMI_IV]
  OK [PUT CDMI w/ 'Content-Type: application/cdmi-container' and 'Accept: application/cdmi-container']
  OK [GET CDMI w/ 'Accept: application/cdmi-container']
  OK [GET CDMI w/o 'Accept']
  OK [GET CDMI w/ 'Accept: */*']
  OK [DELETE CDMI]
-OK ContainersCDMI_IV [gr.grnet.cdmi.client.tests.ContainersCDMI_IV]
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
