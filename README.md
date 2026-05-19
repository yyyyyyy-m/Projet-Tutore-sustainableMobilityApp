# GreenGo — Application Android de mobilité durable

GreenGo est une application mobile Android développée dans le cadre d’un projet tutoré. Elle vise à encourager une mobilité plus responsable en proposant des fonctionnalités de covoiturage, de recherche d’itinéraires et de suivi de trajets écoresponsables.

Le projet combine une approche technique, avec le développement d’une application mobile connectée à Firebase et Google Maps, et une approche MIAGE orientée gestion de projet, modèle économique, plan de financement initial et stratégie de lancement.

---

## Sommaire

- [Présentation du projet](#présentation-du-projet)
- [Objectifs](#objectifs)
- [Fonctionnalités principales](#fonctionnalités-principales)
- [Technologies utilisées](#technologies-utilisées)
- [Architecture du projet](#architecture-du-projet)
- [Installation et lancement](#installation-et-lancement)
- [Configuration Firebase et Google Maps](#configuration-firebase-et-google-maps)
- [Structure du projet](#structure-du-projet)
- [Dimension business](#dimension-business)
- [Améliorations possibles](#améliorations-possibles)
- [Auteurs](#auteurs)

---

## Présentation du projet

GreenGo est une application Android destinée aux étudiants et jeunes actifs souhaitant organiser leurs déplacements de manière plus durable. L’application permet notamment de rechercher, publier et réserver des trajets de covoiturage, tout en intégrant des services de géolocalisation et de cartographie.

L’idée principale est de proposer une solution simple et accessible pour réduire l’usage individuel de la voiture, favoriser le partage de trajets et sensibiliser les utilisateurs à l’impact écologique de leurs déplacements.

---

## Objectifs

Les objectifs du projet sont les suivants :

- Développer une application mobile Android fonctionnelle et intuitive.
- Faciliter la recherche et la publication de trajets de covoiturage.
- Intégrer une carte interactive pour la sélection des lieux de départ et d’arrivée.
- Utiliser Firebase pour l’authentification, le stockage des trajets et la gestion des réservations.
- Proposer une expérience utilisateur fluide grâce à une navigation par fragments.
- Étudier la viabilité du projet à travers un modèle économique, un plan de financement initial et une stratégie de lancement.

---

## Fonctionnalités principales

### Authentification utilisateur

- Inscription d’un nouvel utilisateur.
- Connexion via Firebase Authentication.
- Gestion de session utilisateur.
- Accès conditionnel à certaines fonctionnalités selon l’état de connexion.

### Covoiturage

- Publication d’un trajet par un conducteur.
- Saisie du lieu de départ, de la destination, de la date, de l’heure, du nombre de places et du prix.
- Recherche de trajets disponibles selon plusieurs critères.
- Affichage des résultats de covoiturage.
- Réservation d’un trajet.
- Mise à jour automatique du nombre de places restantes.
- Changement du statut d’un trajet lorsque toutes les places sont réservées.

### Carte et géolocalisation

- Sélection d’une adresse depuis une carte interactive.
- Recherche d’adresse avec géocodage.
- Récupération des coordonnées GPS du départ et de la destination.
- Affichage des marqueurs sur Google Maps.

### Itinéraires

- Calcul d’un itinéraire en voiture entre deux points.
- Appel à l’API Google Directions.
- Affichage de la distance et de la durée estimée.
- Représentation du trajet sur la carte avec une polyline.
- Confirmation de l’itinéraire avant la publication du trajet.

### Réservation et paiement simulé

- Affichage d’un écran de confirmation de réservation.
- Simulation du paiement d’un trajet de covoiturage.
- Enregistrement de la réservation dans Firestore.
- Mise à jour des informations du trajet réservé.

### Profil et suivi

- Page profil utilisateur.
- Accès aux activités de covoiturage.
- Historique ou suivi des trajets liés à l’utilisateur.

---

## Technologies utilisées

### Développement mobile

- Java
- Android Studio
- Android SDK
- Gradle Kotlin DSL
- XML pour les interfaces Android

### Services Google et Firebase

- Firebase Authentication
- Firebase Firestore
- Google Maps API
- Google Directions API
- Google Play Services Location

### Bibliothèques Android

- AndroidX AppCompat
- Material Components
- ConstraintLayout
- CardView
- Navigation Component

### Réseau et données

- OkHttp
- Retrofit
- Gson Converter

---

## Architecture du projet

L’application suit une organisation Android classique basée sur des activités, des fragments et des ressources XML.

### Activités principales

- `SplashActivity` : écran de lancement.
- `LoginActivity` : connexion utilisateur.
- `InscriptionActivity` : inscription utilisateur.
- `MainActivity` : activité principale contenant la navigation.
- `TrajetActivity` : gestion ou affichage lié aux trajets.

### Fragments principaux

- `AccueilFragment` : écran d’accueil.
- `CarteFragment` : affichage de la carte.
- `PartageFragment` : recherche et publication de trajets de covoiturage.
- `ResultatCovoiturageFragment` : affichage des résultats de recherche.
- `PaiementCovoiturageFragment` : confirmation et simulation du paiement.
- `MapPickerFragment` : sélection d’un lieu sur la carte.
- `DrivingRouteFragment` : calcul et affichage d’un itinéraire.
- `ProfilFragment` : espace profil utilisateur.
- `ActivitesCovoiturageFragment` : suivi des activités de covoiturage.

### Données principales

Les données sont principalement stockées dans Firebase Firestore, notamment :

- les utilisateurs ;
- les trajets publiés ;
- les réservations ;
- les informations liées aux itinéraires.

---

## Installation et lancement

### Prérequis

Avant de lancer le projet, il faut installer :

- Android Studio ;
- JDK 11 ou version compatible ;
- Android SDK avec une version compatible ;
- un émulateur Android ou un appareil Android physique ;
- un projet Firebase configuré ;
- une clé Google Maps API valide.

### Cloner le projet

```bash
git clone https://gitlab.com/Rsainta/votre-projet.git
cd votre-projet
