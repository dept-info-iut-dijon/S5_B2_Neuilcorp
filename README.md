# S5_B2_NeuilCorp

Le projet Spot the Difference! a pour objectif de développer une application mobile multijoueur qui permet aux utilisateurs de jouer au célèbre jeu des "7 erreurs". Les joueurs, répartis sur des écrans séparés, doivent collaborer pour trouver les différences entre deux images. Chaque joueur ne voit qu'une image, et la communication est essentielle pour identifier les différences et progresser dans la partie. 

## Table des matières

    - Objectifs clés
    - Fonctionnalités
    - Technologies utilisés
    - Mise en place
    - Structure du projet


## Objectifs Clés

- Multijoueur asynchrone : Permettre à plusieurs joueurs de participer simultanément à une même partie en communiquant pour localiser les différences. 
- Validation collective des erreurs : Les différences ne seront validées que si tous les joueurs pointent au même endroit, encourageant la coopération. 
- Ajout d'Images par des administrateurs : Les administrateurs du jeu pourront ajouter de nouvelles paires d'images pour enrichir le contenu de l'application. 
- Mode chronométré : Chaque joueur dispose d’un temps limité pour trouver les différences, rendant la partie plus compétitive et engageante. 
- Gestion du Score : Un système de score mesurera la performance des joueurs, avec des récompenses pour les bonnes réponses et des malus pour les erreurs ou le dépassement du temps imparti. 
- Technologies Employées : Le projet sera séparé en une partie Front-End qui sera développée en language Java grace à l'environnement de travail Android Studio, et une partie Back-End qui sera développée en C# avec l'environnement de travail Visual Studio 2022. 


## Fonctionnalités

### Finies :

    - Sélection d'une différence.
    - Validation d'une différence.

### À valider :

    - Affichage du menu d'accueil.
    - Créer une partie en ligne.

### À faire :

    - Rejoindre une partie en ligne.
    - Ajout d'un timer dans les parties multijoueurs.
    - Visualisation du score pendant la partie.
    - Classement personnel.
    - Gestion des images (Admin).
    - Gestion de la sélection d'image dans une partie.


## Technologies utilisés

    - Interface en xml, java et kotlin sur un projet AndroidStudio.
    - Métier en C#.
    - Serveur en .NET avec ASP.NET Core.


## Mise en place

## Si vous voulez utiliser l'application directement

    Vous pouvez télécharger l'apk de l'application ici :
  https://tinyurl.com/apkSTD


## Si vous voulez compiler le projet vous-même

### Interface

    Vous aurez simplement besoin d'une version d'AndroidStudio à jour pour pouvoir le lancer, ainsi que de l'émulateur fourni avec.

### Serveur

    Le serveur ainsi que le back s'ouvre sur VisualStudio par exemple. Vous n'avez pas à lancer quoi que ce soit pour le serveur étant donné qu'une copie de ce dernier est hébergé sur un VPS.


## Structure du projet

```bash
.
├── app
│   ├── build.gradle.kts
│   ├── build.gradle.kts.bak
│   ├── proguard-rules.pro
│   ├── release
│   │   ├── baselineProfiles
│   │   └── output-metadata.json
│   └── src
│       ├── androidTest
│       ├── main
│       └── test
├── build.gradle.kts
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── server
│   ├── API7D
│   │   ├── API7D.csproj
│   │   ├── API7D.csproj.user
│   │   ├── API7D.http
│   │   ├── Controllers
│   │   ├── DATA
│   │   ├── Image
│   │   ├── Metier
│   │   ├── Program.cs
│   │   ├── Properties
│   │   ├── appsettings.Development.json
│   │   ├── appsettings.json
│   │   ├── bin
│   │   ├── obj
│   │   └── objet
│   ├── API7D.sln
│   └── TestsUnitaires
│       ├── TestsControllers
│       ├── TestsMetier
│       ├── TestsObjet
│       ├── TestsUnitaires.csproj
│       ├── bin
│       └── obj
└── settings.gradle.kts
```
