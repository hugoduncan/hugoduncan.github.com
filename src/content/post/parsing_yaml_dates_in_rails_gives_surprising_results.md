---
title: "Parsing YAML Dates in Rails Gives Surprising Results"
pubdate: "2009-03-07"
tags: [ "ruby", "rails", "YAML"]
Description: "Today's surprise was that \"2009-01-01\" and \"2009-1-1\" get parsed differently by the YAML parser in Rails.  The former gets converted to a Date>, while the latter becomes a String.  It confused me for a while, as the problem only showed up when I wanted to send the dates to a Flot chart.  Looking at the standard, it's conforming behaviour.   Must be me that is non-conforming then..."
aliases: ["/post/2009/parsing_yaml_dates_in_rails_gives_surprising_results.xhtml"]
---
<p>Today's surprise was that "2009-01-01" and "2009-1-1" get parsed differently by the <a href="http://www.yaml.org/">YAML</a> parser in <a href="http://rubyonrails.org/">Rails</a>.  The former gets converted to a <code>Date</code>, while the latter becomes a <code>String</code>.  It confused me for a while, as the problem only showed up when I wanted to send the dates to a <a href="http://code.google.com/p/flot/">Flot</a> chart.  Looking at the standard, it's conforming behaviour.   Must be me that is non-conforming then...</p>
