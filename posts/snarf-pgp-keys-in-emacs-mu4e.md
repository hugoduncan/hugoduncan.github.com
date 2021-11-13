
I just moved to [mu][mu] for reading my email.  One feature I was
missing was the ability to receive [PGP][pgp] keys for signed
messages.

When you receive a signed message, `mu` shows the verification status
in the `Signature` field in the message view (see
[MSGV-Crypto][mucrypto]).  If you don't have the sender's PGP key on
your keyring, this will show `unverified`.  Click on the `Details`
link within field will show the sender's key id.  To manually import
the key you can use [`gpg`][gpg]:

```
$ gpg --recv <the-key-id>
```

This seemed a little labourious, so some automation was in order.
`mu4e` allows you to define actions that can be run on messages (or
attachments), so I just wrote an action to do this.

```lisp
(defun mu4e-view-snarf-pgp-key (&optional msg)
  "Snarf the pgp key for the specified message."
  (interactive)
  (let* ((msg (or msg (mu4e-message-at-point)))
          (path (mu4e-message-field msg :path))
          (cmd (format "%s verify --verbose %s"
                 mu4e-mu-binary
                 (shell-quote-argument path)))
          (output (shell-command-to-string cmd)))
    (let ((case-fold-search nil))
      (when (string-match "key:\\([A-F0-9]+\\)" output)
        (let* ((cmd (format "%s --recv %s"
                            epg-gpg-program (match-string 1 output)))
               (output (shell-command-to-string cmd)))
          (message output))))))

```

This works by parsing the output of the `mu` program itself, as
displayed in the `Details` window, to obtain the PGP key id.  It then
executes the `gpg --recv` command, parsing in the parsed key id.

To install the action, we simply add it to `mu4e-view-actions`:

```lisp
(add-to-list 'mu4e-view-actions
             '("Snarf PGP keys" . mu4e-view-snarf-pgp-key) t)
```

[pgp]: http://en.wikipedia.org/wiki/Pretty_Good_Privacy "Pretty Good Privacy"
[gpg]: http://www.gnupg.org/ "GNU Privacy Guard"
[mu]: http://www.djcbsoftware.nl/code/mu/ "mu mail reader"
[mucrypto]: http://www.djcbsoftware.nl/code/mu/mu4e/MSGV-Crypto.html#MSGV-Crypto "mu message cryptography"