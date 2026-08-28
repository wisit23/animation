# Animation Scenario: My Memories
# Description:

Use lines, curves, and include one midpoint circle algorithm or one midpoint ellipse algorithm to create a 600 x 600 animation
Work in pairs
Draw animation in a theme of “MY MEMORIES”
Use Java 2D API only
AT LEAST 5 SECONDS
Submit through Google Classroom only
Submit 1 Java file named “Assignment1_studentID_yourPairID.java” and the captured movie named “Assignment1_studentID_yourPairID.mov” or “Assignment1_studentID_yourPairID.mp4”, for example, Assignment1_67050001_67050002
Write every member’s name and student ID in your group in Google Classroom submission comment
Every “inspired” image must also be referenced in Google Classroom submission comment
Submit before 23.59 of Monday 28 August 2026

# Code Requirements:
- All required features implemented thoroughly with optimal approaches.
- Excellent code style; clear naming, proper indentation, and comments.
- Evident effort beyond minimum requirements

## 1. Project overview

This project is a 600 x 600 Java Swing animation created with the Java 2D API.
It presents a quiet present-day moment that triggers six nostalgic childhood and family memories:
a funny football match, an imaginative toy-sword battle, a happy day playing
in a forest stream, riding bicycles together on a sun-drenched countryside road at sunset,
a cozy evening watching an animated Ultraman vs Godzilla battle on TV with friends,
and a warm family Moo Kratha (Thai BBQ) dinner at home.
The animation uses a hand-drawn anime/stickman style and loops continuously every 43.8 seconds.

The visual story follows this structure:

```text
Present: looking at the night sky (wide & POV)
        -> falls asleep
Memory 1: football match & bicycle kick
        -> memory changes
Memory 2: toy-sword fight & BONK
        -> memory changes
Memory 3: playing in a forest stream
        -> memory changes
Memory 4 (Scene 5): 4 friends riding bicycles at sunset countryside (ref/scene5/1.png)
        -> memory changes
Memory 5 (Scene 6): cozy TV night at home (Ultraman vs Godzilla animated battle) (ref/scene6/1.png)
        -> memory changes
Memory 6 (Scene 7): cozy Moo Kratha dinner with family at home (ref/scene7/1.png)
        -> wakes up in the present (tear of nostalgia flows & zoom out)
```

## 2. Complete timeline

### Scene 1: Present - night stargazing (0.0-6.8 seconds)

The main character lies on a grassy hill at night. His head is on the left side
of the frame and his feet point to the right. Both hands are placed behind his
head, creating a relaxed pose. A contact shadow underneath the body helps the
character feel supported by the hill instead of floating above it.

The environment contains:

- a deep-blue vertical gradient sky;
- a diagonal Milky Way made from translucent curved bands;
- a large moon with glow, craters, and a crisp circular outline;
- many seeded stars with different sizes, colors, and twinkle phases;
- distant mountains and a dark grassy hill;
- swaying foreground grass and moonlit chamomile flowers;
- a dark vignette around the edges of the frame.

The character gently moves up and down to suggest breathing. From 1.0 to 2.5
seconds, a shooting star travels diagonally across the sky. From 2.4 to 3.4
seconds, the camera smoothly zooms toward the character's eye.

At 3.4 seconds, the perspective seamlessly switches into a First-Person POV
(looking directly up into the vast night sky through the character's eyes).
From 4.0 to 4.45 seconds, the character blinks naturally (eyelids gently
close ~50% and reopen) while continuing to gaze peacefully at the starry cosmos.
From 5.6 to 6.5 seconds, the eyelids slowly and gently close down into
peaceful darkness, initiating the nostalgic flashback.

At 6.8 seconds, the scene transitions smoothly into Memory 1.

### Scene 2: Memory 1 - football match (6.8-11.8 seconds)

The first memory takes place on a bright grassy football field. The scene shows
a player running beneath a falling ball and attempting an exaggerated bicycle kick.
A goalkeeper waits on the right side of the field.

The action develops as follows:

- At the beginning, the ball drops from above while the kicker runs into position.
- The player leaps and rotates backward into a bicycle-kick pose.
- At approximately 1.95 seconds into the memory, the foot contacts the ball.
- The ball flies quickly toward the goalkeeper with a visible motion trail.
- The goalkeeper is hit and is carried toward the right side of the screen.
- Speed lines, impact bursts, screen shake, and a short white impact flash emphasize
  the comedic hit.
- After the action, the kicker lands and performs a celebratory SIUUU pose.

At 11.8 seconds, a clean white flash transition moves the animation into the second memory.

### Scene 3: Memory 2 - toy-sword fight (11.8-17.8 seconds)

The second memory is an imaginative childhood battle in a dramatic sunset field.
The background contains a purple-to-orange gradient sky, a glowing sun, distant
mountains, layered hills, embers, and a dark foreground battlefield.

Two children face each other while holding glowing toy swords. One child uses a
blue scarf and cyan sword aura. The other uses a red scarf and red sword aura.

The action is divided into four phases:

1. **Energy charge (0.0-0.6 seconds):** both children hold battle-ready stances
   while colored energy pulses near their bodies.
2. **Dash (0.6-1.4 seconds):** both children rush toward the center with speed
   lines and animated running poses.
3. **Sword clash (1.4-3.0 seconds):** the children exchange rapid slashes. Curved
   slash ribbons, sparks, impact bursts, and screen shake make the toy battle feel
   intentionally exaggerated.
4. **BONK and aftermath (3.0-6.0 seconds):** one child lands a downward hit. The
   comic word “BONK!” appears during the impact. The injured child then cries with
   exaggerated waterfall tears while the other child panics and apologizes.

At 17.8 seconds, a white warp transition opens the stream memory.

### Scene 4: Memory 3 - playing in a forest stream (17.8-22.8 seconds)

The third memory is a warm and energetic childhood scene based on the reference
image in `ref/scene4/1.png`. Four friends are playing together in a shallow stream
inside a bright green forest.

The environment contains:

- a layered green forest canopy;
- several tree trunks and branches framing the left and right sides;
- dappled sunlight filtering through the leaves;
- a shallow blue-green stream occupying the lower half of the scene;
- curved current lines and bright water ripples;
- rocks along both stream banks and large foreground rocks;
- splashing water droplets and bright water highlights.

The four characters have different roles so the scene reads as an active group
memory instead of four identical standing figures:

- **Left friend - splashing:** leans toward the group and throws a stream of water
  toward the center. Droplets follow a curved arc from the hands.
- **Center-back friend - laughing:** stands slightly farther back, raises the arms,
  and splashes cheerfully.
- **Center-front friend - celebrating:** sits in the stream, laughs with an open smile,
  and raises both hands in excitement.
- **Right friend - jumping:** steps onto the riverbank and waves happily.

At 22.8 seconds, a clean white warp transition opens the sunset countryside bicycle riding memory.

### Scene 5: Memory 4 - riding bicycles at sunset countryside (22.8-28.8 seconds)

The fourth memory is a breathtaking and joyful childhood memory based on `ref/scene5/1.png`.
Four friends are riding their bicycles together along a sun-drenched countryside dirt road
at golden hour, bathed in warm sunset light and Komorebi tree shadows.

The environment contains:

- a warm golden-hour sunset gradient sky with glowing sun orb and soft amber clouds;
- distant purple-brown mountain ranges with golden atmospheric haze;
- rustic split-rail wooden fence along the left side with distant farm huts;
- a traditional Japanese countryside house on the right with a weathered wooden signpost (`森の里`);
- majestic arching green trees framing both sides, casting dynamic dappled sunlight (Komorebi);
- a wide countryside dirt/gravel road with textured pebbles, ruts, and roadside wild chamomile flowers;
- animated Komorebi sunbeams, floating golden dust motes, and gently drifting leaves.

The 4 friends and bicycles:

- **Friend 1 (Left - Red Hoodie):** spiky black hair, red hoodie with white graphic block on chest, dark pants, riding a royal blue bicycle, smiling cheerfully with wind blowing his hoodie cords.
- **Friend 2 (Center-Left Background - Blue Jacket):** brown hair, blue jacket with backpack, riding a green bicycle with front basket, pedaling happily in the background.
- **Friend 3 (Center-Right Foreground - Protagonist / White Hoodie "23"):** brown spiky hair, clean white hoodie with bold dark-blue number **"23"**, navy trousers, riding a dark green bicycle with a basket carrying his green duffel bag, joyous smiling face with blushing cheeks.
- **Friend 4 (Right - Green Hoodie):** dark hair, bright green hoodie with double white arm stripes, khaki pants, riding a blue bicycle with black wire basket, looking warmly at Friend 3 and the group.

All bicycles feature rotating wheel spokes, spinning crank/pedals with realistic kinematic leg pedaling,
subtle sinusoidal road vibration bobs, and swaying hair in the breeze.

At 28.8 seconds, a clean white warp transition opens the living room TV show memory.

### Scene 6: Memory 5 - watching TV together at home (Ultraman vs Godzilla) (28.8-34.8 seconds)

The fifth memory is a nostalgic childhood scene based on `ref/scene6/1.png`. Four friends
are gathered together in the living room on a cozy evening, eating popcorn, hugging pillows,
and excitedly watching an animated action battle on television between **Ultraman Stickman**
and a giant **Godzilla Monster**.

The environment contains:

- a warm cream-amber living room wall and polished teak wood floor;
- a large patterned woven living room rug;
- a deep-teal cushioned sofa in the background with colorful throw cushions (yellow, green, orange);
- a floor lamp on the right casting a warm amber glow;
- a wall clock, framed "ADVENTURE" mountain landscape poster, and wooden shelf with family photo & succulent;
- a wooden TV console with game console and glowing green power LED;
- a low wooden coffee table in the foreground spread with snacks:
  - a large royal blue ceramic bowl with golden stars, heaped high with buttery popcorn;
  - a crinkled yellow snack bag;
  - striped soda paper cups with white bent straws;
  - a black TV remote control resting on the tabletop.

The animated TV screen show (`drawTVScreenBattle`):
- a city skyline under blue sky with skyscrapers and glowing window grids;
- **Ultraman Stickman:** silver metallic hero with red body markings, fin crest helmet,
  glowing yellow eyes, and flashing blue color timer on the chest; strikes the iconic
  cross-arm (+) pose to fire the **Spacium Ray (ลำแสงสเปเซียม)** electric cyan-white energy beam;
- **Godzilla Kaiju Monster:** giant roaring dark-green reptilian kaiju with glowing red eyes,
  sharp teeth, and glowing atomic dorsal spines, breathing a roaring orange-red **Atomic Fire Breath**;
- **Clash Explosion:** the two beams collide in mid-air with starburst sparks, energy rings, and explosion pulses;
- **Dynamic TV Glow:** the TV casts pulsating cyan-blue and fiery-orange ambient backlight across the room.

The 4 stickman friends watching TV:
- **Friend 1 (Left - Red Hoodie):** sits on the carpet, holding a popcorn bowl and popping a piece of popcorn into his mouth, smiling at the TV action.
- **Friend 2 (Center - White Hoodie "23" / Protagonist):** sits cross-legged on the carpet hugging a soft green cushion on his lap, with sparkling admiring eyes and joyful open smile.
- **Friend 3 (Right floor - Green Hoodie):** sits on the carpet hugging a large orange throw pillow tightly against his chest, leaning forward in pure suspense.
- **Friend 4 (Back - Blue Hoodie):** sits up high on the sofa, resting his arms on a yellow cushion, laughing and cheering enthusiastically for Ultraman!

At 34.8 seconds, a bright white warp transition opens the home Moo Kratha memory.

### Scene 7: Memory 6 - cozy Moo Kratha dinner at home with family (34.8-40.8 seconds)

The sixth memory is a deeply heartwarming and nostalgic family dining scene based on
the reference image in `ref/scene7/1.png`. Four stickman family members/friends are
gathered around a sizzling Thai Moo Kratha (หมูกระทะ) pan at home in a cozy room with
warm golden-amber lighting.

The environment contains:

- a warm indoor room wallpaper gradient and polished teak wood floor;
- a wooden staircase climbing up in the background;
- framed family photos and cute memo notes on the wall (`กินข้าวยังครับ? ♥`, `สู้ๆ นะ!`, `GOOD LUCK!`);
- a silver refrigerator with colorful bear/apple magnets;
- a wooden bookshelf with colorful books and a trailing potted green plant;
- a warm overhead pendant lamp casting a glowing golden light cone over the table;
- a large wooden dining table spread with delicious food side dishes:
  - stainless steel trays of thinly sliced pink marbled pork belly;
  - dark-glazed marinated pork trays sprinkled with roasted white sesame seeds;
  - thick-cut bacon strips tray;
  - plates of snowy glass noodles;
  - bowls of fiery red Thai Suki/Moo Kratha dipping sauce with sesame and chopped green chili;
  - cold faceted glasses of iced cola with floating ice cubes and bear emblems.

The centerpiece is an intricately detailed, steaming Thai Moo Kratha pot:
- dark burner stand with warm gas flame glow;
- outer stainless steel bowl rim and circular soup moat with simmering golden broth,
  napa cabbage, morning glory greens, enoki mushroom bundles, carrot flower slices,
  and animated simmering bubbles;
- raised domed center grill with grill ridges, melting pork lard at the apex,
  and sizzling sliced pork belly with golden-brown sear marks;
- multi-layer billowing aromatic steam plumes curling upward using smooth cubic Bezier curves.

The 4 stickman characters interact around the table:
- **Friend 1 (Left - Black shirt):** leans forward with chopsticks, reaching into the
  pan to pick up a sizzling pork slice, with an excited wink and wide smile.
- **Friend 2 (Center-Left - White "SUMMER" shirt / Protagonist):** holds chopsticks high
  with a succulent, steaming piece of grilled pork belly right in front of his face,
  blowing on it with blushing pink cheeks and sparkling joyful eyes.
- **Friend 3 (Center-Right - Blue "GOOD VIBES" shirt):** holds a small dipping sauce bowl
  in one hand and chopsticks in the other, laughing wholeheartedly with closed crescent eyes (`^ ^`).
- **Friend 4 (Right - Yellow "SKATE" shirt):** raises high a cold glass of iced cola
  cheering towards the group ("ชนแก้ว!") with a cheerful open smile.

At 40.8 seconds, the scene gently fades into peaceful darkness before returning to
the present night scene.

### Scene 8: Return to the present (40.8-43.8 seconds)

The memory sequence completes and returns to the present stargazing moment in
First-Person POV. From 40.8 to 41.6 seconds, the character's eyelids gently flutter
open to reveal the starry sky once more.

From 41.6 to 42.9 seconds, the camera smoothly zooms back out from the character's
face to the wide composition. As the character smiles peacefully, a glistening tear
of nostalgia forms and trickles down the cheek (drawn with midpoint circle/ellipse
and glistening trail).

From 42.9 to 43.8 seconds, the tear gently fades out into a calm, fulfilled smile,
seamlessly looping back to 0.0 seconds.

## 3. Transitions and visual effects

- The transition from the present into Memory 1 uses a smooth black fade as the
  character closes his eyes.
- The transition between memories uses clean white flashes and warps.
- The transition into Memory 4 (Bicycles) uses a radiant golden-white flash.
- The transition into Memory 5 (TV Show) uses a clean white warp flash.
- The transition into Memory 6 (Moo Kratha) uses a warm white warp flash.
- The transition from Memory 6 back to the present uses a smooth black fade into sleep/waking.
- The football impact and sword “BONK!” moment use separate short white flashes and
  screen shake.
- Stars twinkle, grass moves in the wind, water ripples animate, wheels spin, bicycles bounce, steam billows, soup simmers, and TV screen battles clash.

## 4. Camera and presentation

- The viewport is fixed at 600 x 600 pixels.
- The present scene uses a side-oriented lying pose and a targeted zoom toward the eye.
- The camera transforms the night scene as a complete composition.
- The memory scenes use full-frame action compositions with rich foreground and background depth.
- The first and final transitions use a black fade; the internal memory reveals
  use white warp flashes.
- The animation runs at approximately 60 frames per second and repeats automatically.

## 5. Java 2D and computer-graphics techniques

The program is implemented using Java Swing and Java 2D only. It demonstrates:

- Bresenham line drawing for stickman limbs, bicycle spokes, frame tubes, table edges, chopsticks, TV bezel, buildings, and ray beams;
- DDA line drawing from the graphics laboratory algorithms;
- Bezier curves for hair, faces, mouths, eyelids, grass, waves, steam plumes, atomic fire breath, bicycle handlebars, and slash effects;
- midpoint circle drawing for bicycle wheels, wheel hubs, headlights, the moon, heads, bubbles, cups, popcorn kernels, clock, and energy sparks;
- midpoint ellipse drawing for bicycle pedals, bag cushions, Moo Kratha pan, plates, Godzilla spines, and room carpets;
- `AffineTransform` for the present-scene camera zoom, head rotation, bicycle scaling, and hand gestures;
- alpha blending and gradient paints for atmosphere, lighting, Komorebi sunbeams, water, broth, TV backlight glow, and transitions;
- elapsed-time animation using normalized progress and smooth-step interpolation.

## 6. Story purpose

The animation presents memories as emotionally connected snapshots. The quiet
night scene establishes the present. Closing the eyes becomes the gateway into
childhood and family memories. The six memories deliberately vary in mood:

- football provides comedy and energetic movement;
- the toy battle provides imagination and exaggerated drama;
- the stream scene provides carefree childhood friendship and laughter;
- the bicycle ride provides golden childhood freedom, adventure, and camaraderie;
- the TV show night provides cozy childhood excitement and cheering for heroes;
- the Moo Kratha dinner provides heartfelt warmth and family togetherness.

Returning to the night scene completes the loop and suggests that the character is
remembering these cherished moments while resting peacefully under the starry sky.


