# Release Pager

A small application to demonstrate a more declarative testing strategy defined by Daniel Beskin in this [article](https://blog.daniel-beskin.com/2024-05-09-purify-tests).

[Release-pager](https://scala.monster/design-a-pager/) is an application that periodically scans a list of sourcecode repositories for new releases. When the application finds an update in one of the repositories, it sends a notification to each of the interested subscribers. It's that simple.

This pet project by Pavels Sisojevs has enough components and adequate complexity to attempt to implement the new testing strategy.

## TO-DO List
* The node ID can be given to `TSID.Factory` by defining the tsid.node system property or the TSID_NODE environment variable. Otherwise, the node identifier will be chosen randomly.
* Define the environment variable `SBT_TPOLECAT_RELEASE` to enable the release mode when building distribution artifacts for this project.

## Resources

### Database Primary Keys

* [UUID or Auto Increment Integer / Serial as the Database Primary Key?](https://www.bytebase.com/blog/choose-primary-key-uuid-or-auto-increment/).
* [The best UUID type for a database Primary Key](https://vladmihalcea.com/uuid-database-primary-key/).
* [Generating Time Based UUIDs](https://www.baeldung.com/java-generating-time-based-uuids).
