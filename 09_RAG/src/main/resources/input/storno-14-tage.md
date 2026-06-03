# Prozessbeschreibung: Widerruf einer Bestellung innerhalb von 14 Tagen nach Erhalt

## Ziel

Dieser Prozess beschreibt die Bearbeitung eines Widerrufs durch einen Kunden innerhalb der gesetzlichen Widerrufsfrist von 14 Tagen nach Erhalt der Ware. Ziel ist die ordnungsgemäße Rückabwicklung des Kaufvertrags einschließlich Rücksendung der Ware und Erstattung bereits geleisteter Zahlungen.

## Voraussetzungen

Ein Widerruf ist möglich, wenn:

* die Bestellung an den Kunden ausgeliefert wurde,
* die gesetzliche Widerrufsfrist von 14 Tagen noch nicht abgelaufen ist,
* kein Ausschluss des Widerrufsrechts vorliegt,
* der Kunde den Widerruf eindeutig erklärt hat.

## Prozessablauf

### 1. Eingang der Widerrufserklärung

Der Kunde erklärt seinen Widerruf über ein Kundenportal, per E-Mail, Brief oder ein bereitgestelltes Widerrufsformular.

**Erfasste Informationen:**

* Bestellnummer
* Kundennummer
* Zeitpunkt des Widerrufs
* Kontaktdaten des Kunden
* Betroffene Artikel

### 2. Prüfung des Widerrufs

Das System prüft:

* Existiert die Bestellung?
* Wurde die Ware bereits zugestellt?
* Liegt der Widerruf innerhalb der 14-tägigen Frist?
* Sind die betroffenen Artikel widerrufsfähig?

**Mögliche Ausschlussgründe:**

* Überschreitung der Widerrufsfrist
* Individuell angefertigte Produkte
* Geöffnete Hygieneartikel
* Weitere gesetzlich definierte Ausschlüsse

### 3. Entscheidung

#### Widerruf zulässig

Der Prozess wird fortgesetzt.

#### Widerruf nicht zulässig

Der Widerruf wird abgelehnt.

Der Kunde erhält eine Benachrichtigung mit Angabe des Ablehnungsgrundes.

Der Prozess endet.

### 4. Bestätigung des Widerrufs

Der Kunde erhält eine Widerrufsbestätigung.

Die Bestätigung enthält mindestens:

* Bestellnummer
* Eingangsdatum des Widerrufs
* Informationen zur Rücksendung
* Rücksendeadresse
* Fristen für die Rücksendung

### 5. Rücksendung der Ware

Der Kunde sendet die Ware an das Unternehmen zurück.

Dabei werden folgende Informationen erfasst:

* Sendungsnummer der Rücksendung
* Eingangsdatum der Ware
* Zugeordnete Bestellung

### 6. Wareneingangsprüfung

Nach Eingang der Rücksendung wird geprüft:

* Ist die Ware vollständig?
* Entspricht die Ware den zurückgesendeten Artikeln?
* Liegen Beschädigungen oder Gebrauchsspuren vor?
* Sind Zubehör und Dokumentation vorhanden?

Das Ergebnis der Prüfung wird dokumentiert.

### 7. Erstattung

Nach erfolgreicher Prüfung wird die Erstattung veranlasst.

Das System führt folgende Aktionen aus:

* Ermittlung des zu erstattenden Betrags
* Erstellung einer Gutschrift
* Auslösung der Rückzahlung über das ursprüngliche Zahlungsmittel
* Aktualisierung des Bestellstatus

### 8. Dokumentation

Folgende Informationen werden revisionssicher gespeichert:

* Bestellnummer
* Kundennummer
* Datum des Widerrufs
* Datum des Wareneingangs
* Ergebnis der Wareneingangsprüfung
* Erstatteter Betrag
* Zeitpunkt der Rückzahlung

### 9. Benachrichtigung des Kunden

Der Kunde erhält eine Bestätigung über die erfolgte Rückabwicklung.

Die Benachrichtigung enthält mindestens:

* Bestellnummer
* Erstatteten Betrag
* Datum der Rückzahlung

### 10. Prozessende

Die Bestellung befindet sich im Endstatus `WIDERRUFEN`.

Alle erforderlichen Rückabwicklungsmaßnahmen wurden durchgeführt und dokumentiert.

## Ergebnis

Der Kaufvertrag wurde innerhalb der gesetzlichen Widerrufsfrist wirksam widerrufen. Die zurückgesendete Ware wurde geprüft, die Zahlung erstattet und die Bestellung ordnungsgemäß abgeschlossen.
