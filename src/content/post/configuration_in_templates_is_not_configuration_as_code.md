---
title: "Configuration in Templates is not Configuration as Code"
Pubdate: "2010-10-04"
tags: [ "system", "configuration", "pallet", "devops", "templating" ]
Description: "If you have configuration that uses template configuration files, you are practising neither configuration as code nor configuration as data.  Having configuration locked away in template files reduces its visibility, and makes it hard for you to query it. It might be easier to write configuration code to use templates, but it will come back to bite you."
aliases: ["/post/2010/configuration_in_templates_is_not_configuration_as_code.xhtml"]
---
<p>If you have configuration that uses template configuration files, you are practising neither configuration as code nor configuration as data.  Having configuration locked away in template files reduces its visibility, and makes it hard for you to query it. It might be easier to write configuration code to use templates, but it will come back to bite you.</p>

<p>One of the first things I implemented in <a href="http://github.com/hugoduncan/pallet">pallet</a> was a templating mechanism, because configuration management tools use templates, right?  I even built a template selection mechanism, just like <a href="http://wiki.opscode.com/display/chef/Templates">Chef's</a>.</p>

<p>I have come to realise however, that having configuration in template files is not particularly useful. There are three major problems you are likely to encounter.  Firstly template files are not visible, secondly you can not query the data in the template files, and lastly you will need to touch multiple files to add or modify parameters.</p>

<p>Visibility at the point of usage is important, especially in a team environment.  If you have to find the template file and look at its content when reading your configuration code, then the chances are you assume it hasn't changed, and skip the contents. Making an analogy to the world of software development, templates are like global variables in one sense. You can change the operation of a program with a global variable modified in some obscure place, and in the same way, you can change your system configuration by changing a template file, tucked away in some folder, and not visible from where you are actually calling your configuration crate/recipe.</p>

<p>The ability to query configuration settings allows not just finding out, for example,  which directory a log file is in, but also enables you to put tools on top of your configuration data.  Template configuration files suffer on two counts here - they are separate text files that require parsing to be read, and the format of each configuration file is different.</p>

<p>The last point concerns the flexibility of your configuration. If you have used template files, with hard coded parameter values, and you then want to modify your configuration to dynamically set one of those hard coded values, you have to modify all the specialised versions of the existing templates, and specify the value in code. You have to touch multiple files - lots of room for making typos.</p>

<p>My goal for pallet then, is to have all configuration supplied as arguments to crates.  For most packages a hash map is sufficient an abstraction for providing the data, but when this gets too cumbersome, we'll use a DSL that mirrors the original configuration file language.</p>

<p>Goodbye hidden configuration!</p>
