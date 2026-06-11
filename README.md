<p align="center">
  <img src="icon.png" width="120" alt="">
</p>

<h1 align="center">Rouxen</h1>
<p align="center"><i>on-device network &amp; traffic toolkit for Android</i></p>

```
core............ online
site_analyzer... dns / ssl / headers / ping / ports / whois / traceroute / map
wifi_scanner.... wifi + ble radar
device_monitor.. cpu / memory / battery / storage
traffic_monitor. vpn capture (processes / connections / remote / devices)
apps............ installed app browser + launcher
```

Everything below runs locally, against whatever network the phone is
currently on. No accounts, no telemetry, no remote backend — the only
outbound calls are the ones a given check needs to make (DNS lookups,
WHOIS, IP geolocation).

## site_analyzer

Drop in a host or URL and Rouxen runs it through a set of read-only
checks, each in its own tab:

- `dns` — A, AAAA, MX, NS, TXT, CNAME, resolved via dnsjava against
  8.8.8.8 / 1.1.1.1
- `ssl` — certificate subject, issuer, validity window, days until
  expiry, full chain
- `headers` — status code, redirect chain, and a pass/fail check on
  Strict-Transport-Security, Content-Security-Policy, X-Frame-Options,
  X-Content-Type-Options, X-XSS-Protection, Referrer-Policy and
  Permissions-Policy
- `ping` — ICMP latency over a few attempts, falls back to a raw TCP
  probe if ICMP is filtered
- `ports` — sweep of FTP, SSH, SMTP, DNS, HTTP, HTTPS, MySQL,
  PostgreSQL and the HTTP/HTTPS alt ports
- `whois` — starts at whois.iana.org and follows the referral to the
  registry that actually holds the record
- `traceroute` — hop-by-hop path to the target
- `map` — IP geolocation via ip-api.com, plotted on a static map

Every run is written to a local history (Room) and can be exported as
JSON from the share sheet.

## wifi_scanner

Passive radar for what's broadcasting nearby.

- `wifi` — SSID, BSSID, signal strength, band (2.4 / 5 / 6 GHz),
  security type (open / WEP / WPA / WPA2 / WPA3)
- `ble` — name, address, RSSI, rough distance estimate, manufacturer
  ID, TX power, advertised service UUIDs

Tap an entry to see the full record.

## device_monitor

Live readout of the hardware underneath:

- CPU usage and per-core clock speeds
- memory: used / available / total, low-memory flag
- battery: level, health, temperature, voltage, charging state
- storage usage
- device model, Android version, SDK level, thermal status

## traffic_monitor

A local `VpnService` captures traffic on-device — the VPN interface
never leaves the phone, it's just a hook for inspection. The only
outside calls are IP lookups for the remote tab.

- `processes` — per-app traffic totals
- `connections` — live connection list
- `remote` — destination IPs with geo / ASN info
- `devices` — other hosts seen on the local network

## apps

List of installed apps with live search by name or package, tap to
launch.

## stack

Kotlin, Jetpack Compose + Material3, multi-module (`core:*` /
`feature:*`), Koin for DI, Room for scan history, dnsjava for DNS,
OkHttp/Retrofit + Gson for HTTP and IP lookups. Dark theme by default,
JetBrains Mono throughout, 1px borders and 4dp corners — built to look
like a terminal, not a dashboard.

## build

```bash
./gradlew assembleDebug         # debug APK
./gradlew assembleRelease       # release APK
./gradlew test                  # unit tests
./gradlew connectedAndroidTest  # instrumented tests
./gradlew check                 # lint + tests
```

## permissions

- Location is required by Android to return WiFi/BLE scan results —
  Rouxen doesn't use it for anything beyond that.
- `traffic_monitor` needs the standard VPN consent prompt to set up its
  capture interface.
- The network checks in `site_analyzer` (port scan, traceroute, ping)
  are read-only probes. Point them at hosts and networks you're
  authorized to test.
