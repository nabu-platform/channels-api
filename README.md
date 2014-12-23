# Channels

Where data transactions is merely concerned with providing a log-based facsimile to actual transactions, channels uses that concept and actually allows you to move data around.

A channel is a configured pathway in or out of the system (or both directions) for data. While it sounds simple, there are actually a lot of considerations to be made.

## Basics

To start we need to know two concepts:

- **provider**: a channel provider is a bit of code that knows a specific protocol and can move data around on that particular protocol
- **channel**: the channel itself is merely a configurational construct that allows us to run a specific provider with specific settings

The configuration of a simple directory poller looks like this:

```xml
<channels xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<provider>
		<name>file+dir</name>
		<providerClass>be.nabu.libs.channels.resources.DirectoryInProvider</providerClass>
		<properties xsi:nil="true" />
	</provider>
	<channel>
		<providerId>file+dir</providerId>
		<context>someContext</context>
		<direction>IN</direction>
		<transactionality xsi:nil="true" />
		<priority>0</priority>
		<retryAmount>0</retryAmount>
		<retryInterval>0</retryInterval>
		<finishAmount>0</finishAmount>
		<continueOnFailure>false</continueOnFailure>
		<properties xsi:type="be.nabu.libs.channels.resources.simple.SimpleDirectoryProperties">
			<uri>file:/path/to/somewhere</uri>
			<principal xsi:nil="true" />
			<mustExist xsi:nil="true" />
			<processedDirectory>../processed</processedDirectory>
			<deleteOriginal xsi:nil="true" />
			<processedExtension xsi:nil="true" />
			<fileRegex>.*\.txt</fileRegex>
			<recursive>false</recursive>
			<directoryRegex xsi:nil="true" />
		</properties>
	</channel>
</channels>
```

Ignoring all the additional settings in the channel part, the interesting bits are the provider id -which identifies which provider to run- and the properties that are given to said provider.

It is up to a ChannelOrchestrator to actually execute the channel provider taking into account all the below considerations. 

## Context

The channel context *can* be a unique identifier but it can also be a group of channels. Additionally it follows a dot-based notation that allows you to group multiple channels.

For example you could have a channel called `my.channels.channel1` and a channel called `my.channels.channel2` and then run the context `my.channels` which will actually run both channels in the background.
Alternatively you could also simply call both `my.channels` to the same effect.

The boolean `continueOnFailure` will determine in such a multi-channel scenario whether an error in a channel should block further execution.

The default channel orchestrator requires a contextual writable data store that takes a string as context.
For each channel it runs, it will scope the datastore to the context of the channel. This means you can redirect your data per channel or per group of channels to a different backend.
Proper contextual naming is of course key. A possible approach could be `<application>.<process>.<name>`
That way you can redirect all the data for a given application to a certain backend or all the data for a process or even as specific as a channel.

## Batch Management

The above channel is a directory poller that picks up all files that end with ".txt". There could be more than one text file in that directory though.

If the directory contains two files, two data transactions will be started. They will obviously have unique ids but they will still be linked to one another through a batch id.
This is both for convenience and for those very rare cases where you need to know which data transactions belong to the same batch.

Given the above fact that one call to a channel orchestrator can end up running multiple channels means we have to choose for batch management: one batch across all channels or one batch per channel.
The default orchestrator allows you to choose this.

## Session Management

Suppose you are doing the same polling action as described in the example but to a remote FTP server. It is imperative for performance reasons to reuse the FTP connection for both the scanning and the actual file pickups, otherwise the process will crawl to a halt (tenfold slowdown in tests).

Making sure that the only one such connection exists and that it is properly closed is currently left up to the providers. The default resource-based channel provider handles this internally.

## Partial Processing

Suppose you have a database select as the incoming channel and suppose it selects 2 million records over 5 minutes and streams them into separate data objects of 1000 records each.
It would be nice if you could start processing the first 1000 while the select is still pulling in the other records.

The property `finishAmount` allows you to start processing channel results before the channel is actually done processing everything. If it is set to 0, the next step will only be started once the entire channel is done processing.

## Retry

Some pathways are unstable, e.g. you might connect to a remote host that is notoriously unreliable due to network issues.
The retry parameters allow you to configure how many times a particular channel should be retried (and the interval at which it should be retried) before it is considered failed.

## Variable Properties

In a lot of cases you need to poll variable channels, for example you might need to poll a directory that has a date in the name. Suppose for example that you want to always scan the folder for yesterday on a particular path, you could do set the path to `test/${format(now() - '1d', 'yyyy/MM/dd')}/dir`

This will generate a path with the given format for the date of yesterday.

In some cases you actually need a range of variable channels, for example you might want to poll all the directories of the last 10 days, you could do configure `test/${range(now() - '10d', now(), '1d', 'yyyy/MM/dd')}/dir` which will actually generate 11 channel instances in the background (11 because both start and end are inclusive) and run all of them. 

To go one step further, you can combine such "list" functions to generate a cross section of all possibilities, for example you could set `test/${range(now() - '2y', now(), '1y', 'yyyy')}/${range(now() - '2d', now(), '1d', 'MM/dd')}/dir` which will generate 3x3 folders, one for each combination of the generated years and generated days.

For example if you run this on 2014/12/19, all these folders will be scanned:

```
test/2012/12/17/dir, test/2012/12/18/dir, test/2012/12/19/dir, 
test/2013/12/17/dir, test/2013/12/18/dir, test/2013/12/19/dir, 
test/2014/12/17/dir, test/2014/12/18/dir, test/2014/12/19/dir
```

You can also use this feature to extract common properties to a central configuration. For example if you have multiple webservice channels set up hitting the same server, you can configure the endpoint somewhere centrally and set the endpoint in the channel to `${setting('myEndpoint')}/webservice1`

## Recovery

The channel orchestrator is also responsible for recovery based on data transactions. You can set a custom selector that allows you to choose whether or not a dubious transaction should be retried.
Three phase transactions can be automatically recovered and do not require a selector.

## Who does what?

If we take the process that is described in data transactions:

```java
// Log that you are going to attempt a pick up
transaction.start();
uri = provider.copyToDatastore();
// Persist the URI
transaction.commit(uri);
// At this point the original data still exists and the copied data is in the database
provider.tryToDeleteOriginal();
startTheNextStage();
// Indicate that whatever process does something with the data has been started so the transaction is done from its point of view
// Whether or not the resulting process fails is not a concern for the data transaction
transaction.done();
```

The `ChannelProvider` gets a `DataTransactionBatch` object and should do this:

```java
transaction = batch.start();
try {
	uri = copyDataToDatastore();
	transaction.commit(uri);
}
catch (Exception e) {
	transaction.fail(e.getMessage());
}
```

If your provider can perform a cleanup of the original data to ensure that it is not picked up again, it should implement `TwoPhaseChannelProvider` which mandates an additional `finish()` method that should clean up the data that was picked up (set a flag, delete it, move it,...). Keep in mind that the finish method can be called multiple times for recovery reasons.
Your twophase provider **should not** call the finish method himself.

The channel orchestrator is responsible for calling `ChannelUtils.manage()` to create wrap a layer around the `DataTransactionBatch` which will intercept the commit on a handle and perform several steps:

```java
// Still obviously do the actual commit
original.commit();
// Call the finish if we are dealing with a two phase provider
if (channelProvider instanceof TwoPhaseChannelProvider)
	channelProvider.finish();
// Pass the result to the handler
resultHandler.handle(result);
```

This only leaves the `done()` that is not yet called by anyone. There are two ways to handle the results from a channel provider.

## Handling the results

The `ChannelOrchestrator` requires a `ChannelResultHandler` which will -depending on configuration- get the handles to 1 or more data transactions per call of its method `handle()`.

You can implement this interface but then you **must** call the `done()` method on the handle yourself to finish the transaction.

Alternatively you can implement `SingleChannelResultHandler` which will give you exactly 1 data transaction (**not** a handle to one) which you can wrap using `ChannelUtils.newChannelResultHandler()`. It will be wrapped in a default result handler that will call `done()` or `fail()` as necessary.

## Example

Suppose we want to actually run the channel we have defined in the configuration above. Note that you need the channel-resources implementation to run this specific example.

First let's define a very simple result handler that will do something with the resulting data:

```java
public class ExampleChannelResultHandler implements SingleChannelResultHandler {
	@Override
	public void handle(DataTransaction<?> transaction) {
		System.out.println("Received data: " + transaction.getResponse());
	}
}
```

Next, given the path in the config `file:/path/to/somewhere`, drop one or more files in there with a ".txt" extension (or any extension really, just make sure it matches the configured fileRegex).

Then we need a bit of code. Most of this code should be globally set up (like the datastore, the transaction provider etc) but let's go through all the steps:

```java
// We need a datasource to connect to the database
java.sql.DataSource datasource = ...;

// The datastore where we will store the incoming data
ContextualWritableDatastore<String> datastore = new ResourceDatastore<String>();
// Let's set a URN manager to map URLs to URNs
datastore.setUrnManager(new DatabaseURNManager(datasource, TimeZone.getDefault(), "com.mycompany.eai"));

// We need a data transaction implementation
DataTransactionProvider transactionProvider = new DatabaseTransactionProvider(datasource, TimeZone.getDefault());

// We need to load our channel configuration
InputStream configurationData = ...
ChannelManagerConfiguration configuration = new ChannelResourceConfiguration().unmarshal(configurationData);

// We need an orchestrator
ChannelOrchestrator orchestrator = ChannelUtils.newOrchestrator(datastore, transactionProvider);

// Now we need to run the channel
orchestrator.transact(
	ChannelUtils.newChannelManager(configuration),
	"someContext", 
	Direction.IN, 
	ChannelUtils.newChannelResultHandler(new ExampleChannelResultHandler())
);
```

At this point you should see several things. The most visible is that for each file in the folder you have some output in your console stating:

```
Received data: urn:com.mycompany.eai:<yyyy/MM/dd>-<uuid>
```

The files on your file system have been moved to `file:/path/to/processed`.

If you then check your database, you can do `select * from urns` which yields you one entry per picked up file. You can see the URNs outputted to your console and their corresponding URLs.

Next run `select * from data_transactions` which shows you one data transaction per picked up file, all in the state DONE with transactionality set to THREE_PHASE (this is the default).
The response in the datatransactions will match the URNs you see in your console.

Lastly you can run `select * from data_transaction_properties` which will show you the properties per picked up file.