# NiFi Processor for LDES Components

This repository contains the Nifi processor (LdesClient) that wraps the LDES client.


## Build the Processors

To build the project run the following maven command:

```maven
mvn clean install
```

This will build the processor NAR file (Nifi archive).
When the NAR file is placed in the `lib` folder of your NiFi installation, you can add the processor in your workflow.

## Using the Components 

The NiFi Archive will contain multiple LDES NiFi Processors. Below follows a short description how these can be used.

### LDES Client

The main goal for the LDESClient is to replicate an LDES and then synchronize it.

This is achieved by configuring the processor with an initial fragment url. When the processor is triggered, the fragment will be processed and all relations will be added to the (non-persisted) queue.

As long as the processor is running, a queue is maintained that accepts new fragments to process.
The processor also keeps track of the mutable and immutable fragments that have already been processed.

When an attempt is made to queue a known immutable fragment, it will be ignored.
Fragments in the mutable fragment store will be queued when they're expired. Should a fragment be processed from a stream that does not set the max-age in the Cache-control header, a default expiration interval will be used to set an expiration date on the fragment.

Processed members of mutable fragments are also kept in state. They are ignored if presented more than once.


#### Parameters

When running from a docker container, these arguments can be passed through your .env file.
They should be replaced with proper values in the nifi workflow file that contains your process group.

* **TREE_DIRECTION**: makes the direction in which to follow the LDES configurable. Currently not implemented.
* **DATA_SOURCE_URL**: the URL of the LDES to follow.
* **DATA_SOURCE_FORMAT**: the data format of the LDES. This value must be recognizable by the RDFLanguages parser.
* **DATA_DESTINATION_FORMAT**: the data format to use when sending out LDES members. This value must be recognizable by the RDFLanguages parser.
* **DEFAULT_FRAGMENT_EXPIRATION_INTERVAL**: when the LDES source doesn't define a max-age in the Cache-control header, use this value to add to the current system time to generate a reasonable expiration date for the (mutable) fragment.