Akka streams
===============================

In this example we’ll use Akka Streams to ingest a CSV file which contains records of all flight data in the US for a
single year, process the flight data, and emit an ordered list of average flight delays per carrier in a single year.
The code first downloads the CSV file from [here](http://stat-computing.org/dataexpo/2009/2008.csv.bz2) and saves to
`/tmp/flight-data`.

Blog [Diving into Akka Streams](https://medium.com/@kvnwbbr/diving-into-akka-streams-2770b3aeabb0#.fba5qsw4m).

Code mostly copied from [here](https://github.com/rocketpages/flight_delay_akka_streams/blob/master/src/main/scala/sample/stream/FlightDelayStreaming.scala)
with some enhancements.

