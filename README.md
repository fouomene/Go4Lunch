# Go4Lunch

Trouvez un restaurant pour déjeuner avec vos collègues

OpenClassrooms Développeur d'application - Android 6ème projet

Enoncé :

L’application Go4Lunch est une application collaborative utilisée par tous les employés.
Elle permet de rechercher un restaurant dans les environs, puis de sélectionner celui de son choix en en faisant part à ses collègues.
De la même manière, il est possible de consulter les restaurants sélectionnés par les collègues afin de se joindre à eux.
Un peu avant l’heure du déjeuner, l’application notifie les différents employés pour les inviter à rejoindre leurs collègues.

Back-end

Pour fonctionner correctement, l’application mobile a besoin de dialoguer avec un serveur, plus communément appelé back-end.
Afin de simplifier l’implémentation, Go4Lunch se repose sur le back-end Firebase proposé par Google.

Connexion

L’accès à l’application est restreint : il est impératif de se connecter avec un compte Google ou Facebook.

Écran d'accueil

L’application est composée de trois vues principales, accessibles grâce à trois boutons situés en bas de l’écran :
La vue des restaurants sous forme de carte ;
La vue des restaurants sous forme de liste ;
La vue des collègues qui utilisent l’application.
Une fois l’utilisateur connecté, l’application affiche par défaut la vue des restaurants sous forme de carte.

Vue des restaurants sous forme de carte

L’utilisateur est automatiquement géo-localisé par l’application, afin d’afficher le quartier dans lequel il se trouve.
Tous les restaurants des alentours sont affichés sur la carte en utilisant une punaise personnalisée.
Si au moins un collègue s’est déjà manifesté pour aller dans un restaurant donné,
la punaise est affichée dans une couleur différente (verte).
L’utilisateur peut appuyer sur une punaise pour afficher la fiche du restaurant, décrite plus bas.
Un bouton de géolocalisation permet de recentrer automatiquement la carte sur l’utilisateur.

Vue des restaurants sous forme de liste

Cette vue permet d’afficher le détail des restaurants qui se situent sur la carte.

Fiche détaillée d'un restaurant

Lorsque l’utilisateur clique sur un restaurant (depuis la carte ou depuis la liste),
un nouvel écran apparaît pour afficher le détail de ce restaurant.

Liste des collègues

Cet écran affiche la liste de tous vos collègues, avec leur choix de restaurant.
Si un collègue a choisi un restaurant, vous pouvez appuyer dessus pour afficher la fiche détaillée de ce restaurant.

Fonctionnalité de recherche

Sur les vues des restaurants, une loupe située en haut à droite de l’écran permet d’afficher une zone de recherche. 
Cette recherche est contextuelle, et met automatiquement à jour les données de la vue correspondante. 
La recherche s'effectuera uniquement sur les noms des restaurants.

Menu

En haut à gauche se situe un bouton de menu. En cliquant dessus, un menu latéral s’affiche.

Notifications

Un message de notification devra être automatiquement envoyé à tous les utilisateurs
qui ont sélectionné un restaurant dans l'application. Le message sera envoyé à 12h.
Il rappellera à l'utilisateur le nom du restaurant qu'il a choisi, l'adresse, ainsi que la liste des collègues qui viendront avec lui.

Traduction

Vos collègues étant de toutes les nationalités, vous devrez a minima proposer une version française et anglaise de l’application.

Fonctionnalité complémentaire

Trier les restaurants suivant différents critères : proximité, nombre d'étoiles, nombre de collègues.

Icon launcher made by Freepik from www.flaticon.com.

Jean-Pierre Zingraff
