# Gilded Rose — démarche de refactoring

Temps passé :environ 2h

## Objectif

Deux choses à la fois, dans cet ordre : rendre `GildedRose.updateQuality()` lisible et
extensible sans changer son comportement, puis ajouter la règle des articles
« Conjured ».

Contraintes respectées : la classe `Item` et le champ `items` de `GildedRose` ne sont
pas modifiés, conformément à l'énoncé du kata. Le niveau de langage déclaré dans le
`pom.xml` (Java 8) est respecté, sans dépendance ajoutée — seul JUnit 5, déjà présent,
est utilisé.

## Point de départ

La méthode d'origine faisait 52 lignes avec 8 niveaux d'imbrication. Ses principaux
défauts :

- conditions négatives imbriquées (`!name.equals(...)`) jusqu'à 5 niveaux ;
- noms d'articles en chaînes littérales répétées (« Sulfuras » 4 fois, « Backstage »
  3 fois) : une faute de frappe compilait sans erreur ;
- nombres magiques : `50` cinq fois, `11`, `6`, `0` ;
- garde `quality < 50` dupliquée 5 fois, `quality > 0` 2 fois : l'invariant de la
  qualité était éparpillé ;
- `quality = quality - quality`, écriture obscurcie de `quality = 0` ;
- la règle d'un même article était coupée en deux blocs séparés par le décrément de
  `sellIn` — impossible de lire une règle d'une traite ;
- un seul test, qui échouait volontairement.

Un piège mérite d'être signalé, car c'est là que les refactorings de ce kata cassent :
le test de péremption a lieu **après** le décrément de `sellIn`. Un article qui entre
dans la journée avec `sellIn = 0` subit donc déjà la dégradation doublée le jour même.

## Démarche

Six commits, un par intention, chacun laissant la suite de tests verte.

| Commit | Contenu |
|---|---|
| `test:` | filet de sécurité : golden master + tests de caractérisation |
| `refactor:` | constantes nommées (noms d'articles, bornes, seuils) |
| `refactor:` | boucle `for-each`, extraction de méthodes, conditions positives |
| `refactor:` | une stratégie de mise à jour par type d'article |
| `feat:` | règle « Conjured », écrite en TDD |
| `refactor:` | renommage `Items` → `ItemQuality`, `isExpired` portée par l'interface |

### 1. Le filet de sécurité d'abord

Aucune ligne de production n'a été touchée avant d'avoir des tests. Deux niveaux,
deux rôles :

- **Golden master** (`GoldenMasterTest`) : rejoue `TexttestFixture` sur 31 journées et
  compare les 342 lignes de sortie à une référence
  (`src/test/resources/golden-master-30-days.txt`) **capturée sur le code d'origine,
  avant toute modification**. Filet large : il rattrape ce que l'on n'a pas pensé à
  tester.
- **Tests de caractérisation** (`GildedRoseTest`) : une règle métier par test, groupés
  par type d'article avec `@Nested`. Filet précis : quand il casse, il nomme la règle.

Les cas limites sont encadrés des deux côtés de chaque frontière : `sellIn` à
11 / 10 / 6 / 5 / 1 / 0 / −1, qualité à 0 / 1 / 49 / 50, Sulfuras à 80 avec `sellIn`
positif, nul et négatif.

Les deux tests portant sur « Conjured » affirmaient au départ le comportement **buggé**
(−1 point par jour). C'est le principe d'un test de caractérisation : constater, pas
juger. Ils ont été remplacés à l'étape TDD.

Le filet a été validé par une mutation : en changeant le seuil Backstage de
`sellIn < 11` en `sellIn < 10`, le test unitaire correspondant **et** le golden master
échouent. Un filet qu'on n'a jamais vu échouer ne prouve rien.

### 2. Petits refactorings sûrs

Trois pas, du moins risqué au plus risqué, pour que chaque diff soit révisable :

1. **Constantes** : les trois noms d'articles, `MIN_QUALITY` / `MAX_QUALITY`, les deux
   seuils Backstage. `sellIn < 11` devient `sellIn <= 10` — strictement équivalent sur
   des entiers, mais `10` figure dans la spécification alors que `11` n'y figurait pas.
2. **`for-each` et variable locale** : suppression d'une trentaine de `items[i].`.
3. **Extraction et inversion des conditions** : prédicats nommés (`isLegendary`,
   `isExpired`, …), garde en sortie anticipée pour Sulfuras, bornage centralisé dans
   deux méthodes d'une ligne. Résultat : profondeur d'imbrication de 8 à 4 niveaux,
   plus longue méthode de 52 à 12 lignes, zéro condition négative.

### 3. Polymorphisme

Une interface, une implémentation par type d'article, une fabrique qui choisit selon
le nom :

```
ItemUpdater             interface : void update(Item), default isExpired(Item)
├── StandardItemUpdater     article ordinaire     −1/jour, −2 périmé
├── AgedBrieUpdater         Aged Brie             +1/jour, +2 périmé
├── BackstagePassUpdater    billets de concert    +1 / +2 (≤10 j) / +3 (≤5 j), 0 après
├── LegendaryItemUpdater    Sulfuras              aucun changement
└── ConjuredItemUpdater     articles invoqués     −2/jour, −4 périmé

ItemUpdaters            fabrique : nom exact, puis préfixe « Conjured », sinon défaut
ItemQuality             classe compagne d'Item : bornes MIN / MAX, opérations bornées
```

`updateQuality()` devient une délégation de trois lignes :

```java
public void updateQuality() {
    for (Item item : items) {
        ItemUpdaters.forItem(item).update(item);
    }
}
```

Aucun test n'a eu besoin d'être modifié pour cette refonte complète : les tests
s'adressent au comportement via l'API publique, jamais à la structure interne.

### 4. « Conjured » en TDD

Tests écrits d'abord, exécutés et vus échouer (6 rouges sur 7 — le septième, sur le
plancher de qualité, passait déjà, car retirer 1 ou 2 points à une qualité de 1 donne
0 dans les deux cas), puis implémentation.

L'ajout n'a modifié **aucune** des quatre règles existantes : un fichier nouveau
(`ConjuredItemUpdater`, 22 lignes) et une dizaine de lignes dans la fabrique — deux
constantes, une branche et le prédicat `isConjured`. C'est la promesse du principe
ouvert/fermé, tenue.

Le golden master a alors échoué, ce qui est normal : le comportement observable a
changé **volontairement**. Avant de régénérer la référence, le diff a été vérifié :
4 lignes modifiées sur 342, toutes portant le nom « Conjured Mana Cake », aucune
autre. Une seule ligne « Aged Brie » ou « Backstage » modifiée aurait signifié une
régression.

## Décisions et alternatives écartées

**Stratégies plutôt qu'un `switch` sur le nom.** Un `switch` aurait dû être dupliqué :
le comportement se décompose en variation quotidienne et variation après péremption, et
rien n'aurait garanti qu'on ne l'oublie pas dans la seconde. Avec une classe par règle,
les deux moitiés sont dans la même méthode. Ajouter un type devient une addition, pas
la modification d'une méthode testée. Coût assumé : plus de fichiers, et un niveau
d'indirection. Sur quatre types figés le débat serait ouvert ; l'énoncé annonce une
gamme fournisseur, ce qui fait basculer la décision.

**Pas de sous-classes d'`Item`.** L'énoncé l'interdit, mais même autorisé cela ne
fonctionnerait pas : `GildedRose` reçoit un `Item[]` construit à l'extérieur, donc des
sous-classes n'arriveraient jamais jusqu'au système — il faudrait de toute façon une
fabrique lisant le nom. Par ailleurs le type d'un article est porté par un champ
`String` modifiable à l'exécution, alors qu'une classe Java est figée à la
construction. La stratégie, choisie à chaque appel, colle à cette réalité.

**Pas de classe abstraite avec patron de méthode.** Elle supprimerait les deux lignes
(`item.sellIn--` et le test de péremption) répétées dans quatre stratégies, mais
imposerait à chaque règle le découpage « quotidien / périmé » — précisément le
découpage qui rendait le code d'origine illisible. Et `LegendaryItemUpdater` n'entre
pas dans le moule, puisqu'il ne décrémente même pas `sellIn`.

**Pas d'`enum ItemType`.** Une enum est fermée : ajouter un type exigerait de la
modifier, et on perdrait la possibilité de brancher une règle depuis l'extérieur.

**`ItemQuality`, classe utilitaire assumée.** Ses deux méthodes devraient être des
méthodes d'`Item` — c'est lui qui porte l'invariant sur sa propre qualité — mais le
kata interdit d'y toucher. Le choix était entre une classe compagne, sur le modèle de
`Objects` ou `Collections` du JDK, et la duplication de l'invariant dans les cinq
stratégies. Une entorse visible et localisée vaut mieux qu'un invariant dupliqué cinq
fois.

Elle s'est d'abord appelée `Items`. Ce nom, pluriel d'`Item`, laissait croire qu'elle
contenait une collection d'articles ou la liste de leurs noms — un lecteur s'y est
effectivement trompé, ce qui est le seul test d'utilisabilité qui compte pour un nom.
`ItemQuality` annonce le concept, et les membres se lisent alors sans redondance :
`ItemQuality.increase(item)`, `ItemQuality.MIN`.

**`isExpired` portée par l'interface `ItemUpdater`, en méthode `default`.** Le test
`sellIn < 0` ne relève pas de la qualité : le laisser dans `ItemQuality` aurait annulé
le bénéfice du renommage. Trois options étaient ouvertes. L'écrire en ligne dans les
quatre stratégies fait perdre le vocabulaire métier et répète un `0` non nommé. Créer
une classe dédiée pour un unique prédicat d'une ligne coûte plus qu'il ne rapporte.
Le placer sur l'interface met la question au bon endroit : « la date de vente est-elle
dépassée ? » est demandée par *toute* règle de mise à jour, jamais par un client de
`GildedRose`. Les implémentations l'appellent sans qualificateur, `isExpired(item)`, et
la Javadoc de la méthode devient le lieu naturel pour documenter le piège de l'ordre —
le prédicat s'évalue après le décrément de `sellIn`. Limite connue et assumée : une
méthode d'interface appelable depuis les implémentations est nécessairement publique,
donc théoriquement redéfinissable. Les méthodes d'interface `private` introduites en
Java 9 ne répondraient pas au besoin, puisqu'elles ne sont accessibles que depuis
l'interface elle-même.

**Dégradation par pas unitaires.** `ItemQuality.increase` / `ItemQuality.decrease`
avancent d'un point à la fois plutôt que de faire `quality += n` puis d'écrêter. C'est
ce qui rend le refactoring strictement équivalent au code d'origine : une qualité déjà
hors bornes reste intacte, là où un `Math.min(50, …)` l'aurait ramenée à 50. C'est
aussi ce qui garantit le plancher pour « Conjured » : partant d'une qualité de 1, le
premier pas donne 0 et le second ne fait rien.

**« Conjured » reconnu par préfixe, pas par nom exact.** La spécification parle des
« éléments Conjured » et d'un partenariat fournisseur : c'est une gamme, pas un produit.
Un nom exact obligerait à modifier le code à chaque nouveau produit. Dans la fabrique,
le nom exact est testé avant le préfixe, pour qu'un article par ailleurs légendaire
garde sa règle propre.

**Capture de `System.out` dans le golden master** plutôt que modification de
`TexttestFixture` pour lui injecter un `PrintStream`. À l'étape où ce test a été écrit,
il n'existait aucun filet : ne rien modifier était la priorité absolue, et changer la
signature de la fixture risquait de casser le harnais `texttests/` du dépôt. Le coût
est réel et assumé : ce test touche à l'état global de la JVM, donc interdit la
parallélisation.

**Fins de ligne normalisées des deux côtés** dans le golden master (`\r\n` → `\n`). Le
développement s'est fait sous Windows, où `println` produit `\r\n` ; la référence est
stockée en LF. Sans normalisation, le test passerait ici et échouerait sur Linux ou
macOS — la pire catégorie de test, celui qui donne une fausse confiance.

## Comment lancer les tests

Depuis le dossier `Java/`, avec le wrapper Maven (aucune installation requise) :

```
mvnw.cmd -B clean test        (Windows)
./mvnw -B clean test          (Linux, macOS)
```

Résultat attendu : `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0`.

Pour voir la simulation de la fixture, par exemple sur 30 jours (attention au
séparateur de classpath, `;` sous Windows et `:` sous Linux et macOS) :

```
mvnw.cmd -B -q test-compile
java -cp "target/test-classes;target/classes" com.gildedrose.TexttestFixture 30
```

```
./mvnw -B -q test-compile
java -cp target/test-classes:target/classes com.gildedrose.TexttestFixture 30
```

Le projet embarque aussi un wrapper Gradle (`./gradlew test`, `./gradlew -q text`) ;
voir le `README.md` du dossier. Seul Maven a été utilisé ici.

### Régénérer la référence du golden master

À ne faire que lorsqu'un changement de comportement est **voulu**, et seulement après
avoir vérifié le diff ligne par ligne :

```
mvnw.cmd -B -q clean test-compile
java -cp target/test-classes:target/classes com.gildedrose.TexttestFixture 30 > nouvelle-sortie.txt
```

puis comparer avec `src/test/resources/golden-master-30-days.txt` avant de remplacer.
Le fichier est stocké en UTF-8 sans BOM et en fins de ligne LF.

## Vérification au-delà des tests

Les 32 tests servent de boucle de rétroaction rapide, pas de preuve d'équivalence. À
chaque étape structurelle, le code d'origine a été extrait de l'historique Git et
comparé à la version courante sur une grille exhaustive — 8 noms d'articles × `sellIn`
de −10 à 20 × `quality` de −10 à 90, soit 3 131 combinaisons par type, valeurs hors
spécification comprises.

Résultat après l'ajout de « Conjured » :

```
+5 Dexterity Vest                            3131 compares      0 differences
Elixir of the Mongoose                       3131 compares      0 differences
Aged Brie                                    3131 compares      0 differences
Sulfuras, Hand of Ragnaros                   3131 compares      0 differences
Backstage passes to a TAFKAL80ETC concert    3131 compares      0 differences
Conjured Mana Cake                           3131 compares   2748 differences
Conjured Sword of Doom                       3131 compares   2748 differences
(nom vide)                                   3131 compares      0 differences
```

Des différences uniquement là où le métier en a demandé, zéro ailleurs : c'est la
signature d'un ajout de fonctionnalité correct. Ce harnais de comparaison est un outil
jetable, il ne fait pas partie du livrable.

Un seul écart de comportement a été introduit volontairement, et il est signalé ici :
les comparaisons de noms sont écrites `CONSTANTE.equals(item.name)` plutôt que
`item.name.equals(CONSTANTE)`. Sur un `name` à `null`, le code d'origine levait une
`NullPointerException` ; le code actuel traite l'article comme un article ordinaire.

## Pistes d'amélioration

**Retirer le golden master.** C'est un échafaudage, pas un test de valeur durable :
indispensable pour refactorer du legacy sans tests, redondant dès que les tests
unitaires couvrent toutes les règles. Il a de plus le défaut de capturer `System.out`,
ce qui interdit la parallélisation. Il faudrait le supprimer dans un commit dédié,
après avoir mesuré la couverture.

**Rendre la fabrique injectable.** `ItemUpdaters` est statique, ce qui suffit
aujourd'hui. Si l'on avait besoin de règles différentes selon le contexte (un magasin,
une promotion), la `Map` deviendrait une dépendance passée au constructeur de
`GildedRose` — sans toucher aux règles elles-mêmes.

**Décider quoi faire d'un nom inconnu.** Un article dont le nom ne correspond à rien
est traité comme un article ordinaire. C'était le comportement d'origine et il a été
préservé, mais une faute de frappe dans un nom passe donc silencieusement. Sur un
système réel, une alerte ou un rejet serait à discuter avec le métier — c'est un
changement fonctionnel, pas une décision de refactoring.

**Vérifier la qualité de Sulfuras.** Le code ne garantit pas que la qualité d'un objet
légendaire vaut 80 : il se contente de ne jamais y toucher. Un Sulfuras créé à 60
resterait à 60. Là encore, imposer 80 serait un changement fonctionnel à valider.

**Tester les stratégies unitairement.** Chacune est isolée et testable sans
`GildedRose`. Cela n'a pas été fait pour ne pas dupliquer les 32 tests existants, qui
couvrent déjà toutes les règles, et pour ne pas coupler les tests aux noms des classes
internes. Ce serait le chemin à suivre pour remplacer progressivement le golden master.

**Automatiser le test de mutation.** La validation du filet a été faite à la main sur
une seule mutation. Un outil comme PIT le ferait systématiquement et mesurerait la
qualité réelle de la suite de tests, là où la couverture de lignes ne mesure que son
passage.
