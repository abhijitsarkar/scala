Weather Data Parsing
===============================

In this example we’ll use Akka Streams to ingest multiple CSV files obtained from NOAA containing weather data by year.

ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/daily/by_year/
 
The data format is as follows (copied from [ghcn-daily-by_year-format.rtf](ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/daily/by_year/ghcn-daily-by_year-format.rtf)):

> The following information serves as a definition of each field in one line of data covering one station-day. 
> Each field described below is separated by a comma ( , ) and follows the order presented in this document.
>
> ID = 11 character station identification code
> YEAR/MONTH/DAY = 8 character date in YYYYMMDD format (e.g. 19860529 = May 29, 1986)
> ELEMENT = 4 character indicator of element type 
> DATA VALUE = 5 character data value for ELEMENT 
> M-FLAG = 1 character Measurement Flag 
> Q-FLAG = 1 character Quality Flag 
> S-FLAG = 1 character Source Flag 
> OBS-TIME = 4-character time of observation in hour-minute format (i.e. 0700 =7:00 am)
>
> See section III of the GHCN-Daily [readme.txt](ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/daily/readme.txt) file 
> for an explanation of ELEMENT codes and their units as well as the M-FLAG, Q-FLAGS and S-FLAGS.
>
> The OBS-TIME field is populated with the observation times contained in NOAA/NCDC’s Multinetwork Metadata System (MMS).  


