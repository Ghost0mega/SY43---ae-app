# Portail AE UTBM - Projet SY43

Cette application Android est un portail complet pour la vie associative de l'UTBM. Elle permet aux étudiants de suivre les actualités, de consulter le calendrier des événements et de découvrir les différents clubs du campus.

## Fonctionnalités principales

- **Actualités (News)** : Affichage des dernières nouvelles groupées par date avec support du Markdown pour les descriptions.
- **Calendrier** : Vue chronologique des événements du mois pour ne rien rater sur le campus.
- **Annuaire des Clubs** : Liste complète des associations avec détails, membres du bureau et liens vers leurs sites web.
- **Suivi d'événements** : Possibilité de "suivre" des nouvelles spécifiques pour les retrouver facilement dans un onglet dédié.
- **Localisation & Distance** : Calcul en temps réel de la distance entre l'utilisateur et le lieu de l'événement (Maison des Étudiants par défaut).
- **Itinéraire** : Bouton direct pour lancer une navigation GPS vers l'événement.
- **Notifications Intelligentes** : Envoi automatique de rappels discrets **24h** et **1h** avant le début d'un événement suivi.

## Stack Technique & Conformité (Barème)

L'application a été conçue pour respecter strictement les contraintes pédagogiques :

- **Langage** : 100% Kotlin.
- **Interface (UI)** : Développée intégralement avec **Jetpack Compose** et Material 3.
- **Animations** : Utilisation de l'API Animation de Compose pour les transitions entre onglets (HorizontalPager) et les changements de vues (slide-in/out).
- **Base de Données** : Intégration locale via **Exposed (H2)** pour le stockage persistant des news, clubs, membres et préférences de suivi.
- **Capteurs (Sensors)** : Utilisation du **GPS** pour la géolocalisation et le calcul de proximité.
- **Interactions & Multi-Utilisateur** : Système de profil utilisateur avec gestion dynamique des événements suivis.
- **Architecture** : Séparation propre entre la logique Backend (Network, DB, Notification) et l'UI (Screens, Utils).

## Tests

L'application inclut une suite de tests pour garantir sa stabilité :

- **Tests Unitaires** : 
    - `LocationHelperTest` : Validation des calculs géographiques (Haversine).
    - `DateFormattingTest` : Validation du formatage des dates en français.
    - `RepositoryTest` : Validation des interactions SQL (insertions, updates) sur une base de données de test en mémoire.
- **Tests Instrumentés (UI)** :
    - `NewsScreenTest` : Vérification du comportement des composants Compose et de la gestion des états de chargement.

## Utilisation

1. **Navigation** : Utilisez la barre de navigation en bas ou **glissez horizontalement (swipe)** pour changer d'onglet.
2. **Suivi** : Cliquez sur l'étoile d'une news pour la suivre. Elle apparaîtra dans l'onglet "Followed" et vous recevrez des notifications de rappel.
3. **Localisation** : Acceptez la permission GPS au démarrage pour voir votre distance par rapport aux événements.

---
**Développé par :** Luna Hascoet, Amber Guyenot-Cosio, Max Fabre et Alex Cuney (UTBM)
**Cours :** SY43 - Développement d'applications Android
