
<p>Basic Nagios support was recently added to <a href="http://github.com/hugoduncan/pallet">pallet</a>, and while very simple to use, this blog post should make it even simpler. The overall philosophy is to configure the nagios service monitoring definitions along with the service itself, rather than have monolithic nagios configuration, divorced from the configuration of the various nodes.</p>

<p>As an example, we can configure a machine to have it's ssh service, CPU load, number of processes and number of users monitored. Obviously, you would normally be monitoring several different types of nodes, but there is no difference as far as pallet is concerned.</p>

<p>We start by requiring various pallet components.  These would normally be part of a <code>ns</code> declaration, but are provided here for ease of use at the REPL.</p>

<pre class="clojure">
(require
  '[pallet.crate.automated-admin-user
    :as admin-user]
  '[pallet.crate.iptables :as 'iptables]
  '[pallet.crate.ssh :as ssh]
  '[pallet.crate.nagios-config
     :as nagios-config]
  '[pallet.crate.nagios :as nagios]
  '[pallet.crate.postfix :as postfix]
  '[pallet.resource.service :as service])
</pre>

<h2>Node to be Monitored by Nagios</h2>

<p>Now we define the node to be monitored. We set up a machine that has <abbr>SSH</abbr> running, and configure <code>iptables</code> to allow access to <abbr>SSH</abbr>, with a throttled connection rate (six connections/minute by default).</p>

<pre class="clojure">
(pallet.core/defnode monitored
  []
  :bootstrap [(admin-user/automated-admin-user)]
  :configure [;; set iptables for restricted access
              (iptables/iptables-accept-icmp)
              (iptables/iptables-accept-established)
              ;; allow connections to ssh
              ;; but throttle connection requests
              (ssh/iptables-throttle)
              (ssh/iptables-accept)])
</pre>

<p>Monitoring of the <abbr>SSH</abbr> service is configured by simply adding
<code>(ssh/nagios-monitor)</code>.</p>

<p>Remote monitoring is implemented using nagios' <code>nrpe</code> plugin, which we add with <code>(nagios-config/nrpe-client)</code>.  To make nrpe accessible to the nagios server, we open the that the nrpe agent runs on using <code>(nagios-config/nrpe-client-port)</code>, which restricts access to the nagios server node. We also add a phase, :restart-nagios, that can be used to restart the nrpe agent.</p>

<p>Pallet comes with some configured nrpe checks, and we add <code>nrpe-check-load</code>, <code>nrpe-check-total-proces</code> and <code>nrpe-check-users</code>. The final configuration looks like this:</p>

<pre class="clojure">
(pallet.core/defnode monitored
  []
  :bootstrap [(admin-user/automated-admin-user)]
  :configure [;; set iptables for restricted access
              (iptables/iptables-accept-icmp)
              (iptables/iptables-accept-established)
              ;; allow connections to ssh
              ;; but throttle connection requests
              (ssh/iptables-throttle)
              (ssh/iptables-accept)
              ;; monitor ssh
              (ssh/nagios-monitor)
              ;; add nrpe agent, and only allow
              ;; connections from nagios server
              (nagios-config/nrpe-client)
              (nagios-config/nrpe-client-port)
              ;; add some remote checks
              (nagios-config/nrpe-check-load)
              (nagios-config/nrpe-check-total-procs)
              (nagios-config/nrpe-check-users)]
  :restart-nagios [(service/service
                    "nagios-nrpe-server"
                    :action :restart)])
</pre>

<h2>Nagios Server</h2>
<p>We now configure the nagios server node. The nagios server is installed with <code>(nagios/nagios "nagiospwd")</code>, specifying the password for the nagios web interface, and add a phase, :restart-nagios, that can be used to restart nagios.</p>

<p>Nagios also requires a <abbr>MTA</abbr> for notifications, and here we install postfix.  We add a contact, which we make a member of the "admins" contact group, which is notified as part of the default host and service templates.</p>

<pre class="clojure">
(pallet.core/defnode nagios
  []
  :bootstrap [(admin-user/automated-admin-user)]
  :configure [;; restrict access
              (iptables/iptables-accept-icmp)
              (iptables/iptables-accept-established)
              (ssh/iptables-throttle)
              (ssh/iptables-accept)
              ;; configure MTA
              (postfix/postfix
               "pallet.org" :internet-site)
              ;; install nagios
              (nagios/nagios "nagiospwd")
              ;; allow access to nagios web site
              (iptables/iptables-accept-port 80)
              ;; configure notifications
              (nagios/contact
              {:contact_name "hugo"
               :service_notification_period "24x7"
               :host_notification_period "24x7"
               :service_notification_options
                  "w,u,c,r"
               :host_notification_options
                  "d,r"
               :service_notification_commands
                 "notify-service-by-email"
               :host_notification_commands
                  "notify-host-by-email"
               :email "my.email@my.domain"
               :contactgroups [:admins]})]
  :restart-nagios [(service/service "nagios3"
                     :action :restart)])
</pre>

<h2>Trying it out</h2>
<p>That's it. To fire up both machines, we use pallet's <code>converge</code> command.</p>

<pre class="clojure">
(pallet.core/converge
  {monitored 1 nagios 1} service
  :configure :restart-nagios)
</pre>

<p>The nagios web interface is then accessible on the <code>nagios</code> node with the <code>nagiosadmin</code> user and specified password.  Real world usage would probably have several different monitored configurations, and restricted access to the <code>nagios</code> node.</p>

<h2>Still to do...</h2>
<p>Support for nagios is not complete (e.g. remote command configuration still needs to be added, and it has only been tested on Ubuntu), but I would appreciate any feedback on the general approach.</p>