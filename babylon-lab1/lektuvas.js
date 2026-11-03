export const createScene = function () {
  var scene = new BABYLON.Scene(engine);
  scene.clearColor = new BABYLON.Color4(0.68, 0.85, 0.95, 1);
  // HELPER
  scene.createDefaultEnvironment({
    createGround: false,
    createSkybox: true,
    skyboxSize: 100
  });
  // CAMERA
  var camera = new BABYLON.ArcRotateCamera(
    "camera",
    -Math.PI / 2,
    Math.PI / 2.5,
    15,
    new BABYLON.Vector3(0, 1, 0),
    scene
  );
  camera.attachControl(canvas, true);
  // SUN LIGHT
  var light = new BABYLON.DirectionalLight(
    "sunLight",
    new BABYLON.Vector3(-1, -2, -1),
    scene
  );
  light.intensity = 1;
  // SOUND
  var sound = new BABYLON.Sound(
    "sound",
    "sounds/violons11.wav",
    scene,
    null,
    {
      loop: true,
      autoplay: true,
      volume: 0.15
    }
  );
  // MATERIALS
  var redMat = new BABYLON.StandardMaterial(
    "redMat",
    scene
  );
  redMat.diffuseColor = new BABYLON.Color3(
    0.85,
    0.15,
    0.18
  );
  var whiteMat = new BABYLON.StandardMaterial(
    "whiteMat",
    scene
  );
  whiteMat.diffuseColor = new BABYLON.Color3(
    0.92,
    0.92,
    0.95
  );
  var darkMat = new BABYLON.StandardMaterial(
    "darkMat",
    scene
  );
  darkMat.diffuseColor = new BABYLON.Color3(
    0.08,
    0.08,
    0.1
  );
  var blueMat = new BABYLON.StandardMaterial(
    "blueMat",
    scene
  );
  blueMat.diffuseColor = new BABYLON.Color3(
    0.05,
    0.35,
    0.55
  );
  var glassMat = new BABYLON.StandardMaterial(
    "glassMat",
    scene
  );
  glassMat.diffuseColor = new BABYLON.Color3(
    0.15,
    0.3,
    0.4
  );
  glassMat.alpha = 0.7;
  // TEXTURE
  var waterTexture = new BABYLON.Texture(
    "https://assets.babylonjs.com/textures/waterbump.png",
    scene
  );
  blueMat.bumpTexture = waterTexture;
  // AIRPLANE
  var plane = new BABYLON.TransformNode(
    "plane",
    scene
  );
  plane.position.y = 2;
  // CYLINDER - BODY
  var fuselage = BABYLON.MeshBuilder.CreateCylinder(
    "fuselage",
    {
      diameterTop: 0.6,
      diameterBottom: 0.9,
      height: 5,
      tessellation: 16
    },
    scene
  );
  fuselage.rotation.x = Math.PI / 2;
  fuselage.material = redMat;
  fuselage.parent = plane;
  // BOX - WINGS
  var wings = BABYLON.MeshBuilder.CreateBox(
    "wings",
    {
      width: 7,
      height: 0.15,
      depth: 1.2
    },
    scene
  );
  wings.position.z = -0.2;
  wings.material = whiteMat;
  wings.parent = plane;
  // SPHERE - COCKPIT
  var cockpit = BABYLON.MeshBuilder.CreateSphere(
    "cockpit",
    {
      diameter: 0.5
    },
    scene
  );
  cockpit.position.y = 0.45;
  cockpit.position.z = 0.7;
  cockpit.scaling.z = 1.5;
  cockpit.material = glassMat;
  cockpit.parent = plane;
  // TORUS - FRONT RING
  var ring = BABYLON.MeshBuilder.CreateTorus(
    "ring",
    {
      diameter: 0.45,
      thickness: 0.2,
    },
    scene
  );
  ring.rotation.x = Math.PI / 2;
  ring.position.z = 2.5;
  ring.material = darkMat;
  ring.parent = plane;
  // CAPSULE - UNDER BODY
  var capsule = BABYLON.MeshBuilder.CreateCapsule(
    "capsule",
    {
      height: 1.0,
      radius: 0.25
    },
    scene
  );
  capsule.rotation.z = Math.PI / 2;
  capsule.position.y = -0.3;
  capsule.position.z = 0.5;
  capsule.material = darkMat;
  capsule.parent = plane;
  // ICOSPHERE - DETAIL
  var ico = BABYLON.MeshBuilder.CreateIcoSphere(
    "ico",
    {
      radius: 0.3,
    },
    scene
  );
  ico.position.y = 0.35;
  ico.material = darkMat;
  ico.parent = plane;
  // PLANE - VERTICAL TAIL
  var verticalTail = BABYLON.MeshBuilder.CreatePlane(
    "verticalTail",
    {
      width: 1.2,
      height: 1.4,
      sideOrientation: BABYLON.Mesh.DOUBLESIDE
    },
    scene
  );
  verticalTail.position.y = 0.65;
  verticalTail.position.z = -2.35;
  verticalTail.rotation.y = Math.PI / 2;
  verticalTail.material = redMat;
  verticalTail.parent = plane;
  // TUBE - ENGINE EXHAUST
  var exhaust = BABYLON.MeshBuilder.CreateTube(
    "exhaust",
    {
      path: [
        new BABYLON.Vector3(0, 0, 0),
        new BABYLON.Vector3(0, 0, 0.5)
      ],
      radius: 0.15,
      tessellation: 16
    },
    scene
  );
  exhaust.position.z = -2.5;
  exhaust.material = darkMat;
  exhaust.parent = plane;
  // LATHE - REAR DETAIL
  var lathe = BABYLON.MeshBuilder.CreateLathe(
    "lathe",
    {
      shape: [
        new BABYLON.Vector3(0, 0, 0),
        new BABYLON.Vector3(0.12, 0, 0.1),
        new BABYLON.Vector3(0.18, 0, 0.3),
        new BABYLON.Vector3(0.12, 0, 0.5),
        new BABYLON.Vector3(0, 0, 0.5)
      ],
      radius: 1,
      tessellation: 12
    },
    scene
  );
  lathe.scaling = new BABYLON.Vector3(
    0.6,
    0.6,
    0.6
  );
  lathe.position.y = 0.1;
  lathe.position.z = -2.55;
  lathe.material = darkMat;
  lathe.parent = plane;
  // GROUND - WATER
  var water = BABYLON.MeshBuilder.CreateGround(
    "water",
    {
      width: 60,
      height: 60,
      subdivisions: 40
    },
    scene
  );
  water.material = blueMat;
  // HORIZONTAL TAIL
  var horizontalTail = BABYLON.MeshBuilder.CreateBox(
    "horizontalTail",
    {
      width: 2.5,
      height: 0.12,
      depth: 0.6
    },
    scene
  );
  horizontalTail.position.y = 0.15;
  horizontalTail.position.z = -2.35;
  horizontalTail.material = whiteMat;
  horizontalTail.parent = plane;
  // PROPELLER
  var propeller = new BABYLON.TransformNode(
    "propeller",
    scene
  );
  propeller.position.z = 2.5;
  propeller.parent = plane;
  // PROPELLER HUB
  var hub = BABYLON.MeshBuilder.CreateSphere(
    "hub",
    {
      diameter: 0.35
    },
    scene
  );
  hub.material = darkMat;
  hub.parent = propeller;
  // PROPELLER BLADE
  var blade1 = BABYLON.MeshBuilder.CreateBox(
    "blade1",
    {
      width: 0.12,
      height: 1.4,
      depth: 0.03
    },
    scene
  );
  blade1.material = darkMat;
  blade1.parent = propeller;
  var blade2 = blade1.clone("blade2");
  blade2.rotation.z = Math.PI / 2;
  blade2.parent = propeller;
  // WHEEL 1
  var wheel1 = BABYLON.MeshBuilder.CreateCylinder(
    "wheel1",
    {
      diameter: 0.4,
      height: 0.18
    },
    scene
  );
  wheel1.rotation.z = Math.PI / 2;
  wheel1.position.x = -1.6;
  wheel1.position.y = -0.7;
  wheel1.position.z = -0.2;
  wheel1.material = darkMat;
  wheel1.parent = plane;
  // WHEEL 2
  var wheel2 = BABYLON.MeshBuilder.CreateCylinder(
    "wheel2",
    {
      diameter: 0.4,
      height: 0.18
    },
    scene
  );
  wheel2.rotation.z = Math.PI / 2;
  wheel2.position.x = 1.6;
  wheel2.position.y = -0.7;
  wheel2.position.z = -0.2;
  wheel2.material = darkMat;
  wheel2.parent = plane;
  // STRUT 1
  var strut1 = BABYLON.MeshBuilder.CreateCylinder(
    "strut1",
    {
      diameter: 0.08,
      height: 0.55
    },
    scene
  );
  strut1.position.x = -1.6;
  strut1.position.y = -0.38;
  strut1.position.z = -0.2;
  strut1.material = darkMat;
  strut1.parent = plane;
  // STRUT 2
  var strut2 = BABYLON.MeshBuilder.CreateCylinder(
    "strut2",
    {
      diameter: 0.08,
      height: 0.55
    },
    scene
  );
  strut2.position.x = 1.6;
  strut2.position.y = -0.38;
  strut2.position.z = -0.2;
  strut2.material = darkMat;
  strut2.parent = plane;

  // =====================================================================
  // PAPILDYMAI PAGAL LABORATORINIO REIKALAVIMUS
  // =====================================================================

  camera.radius = 30;
  camera.upperBetaLimit = 1.5;

  // ---------- 1. PIVOT: lektuvas skrenda ratu aplink pivota ----------
  var flightPivot = new BABYLON.TransformNode("flightPivot", scene);
  plane.parent = flightPivot;     // lektuvas pririsamas prie pivoto
  plane.position.x = 10;          // 10 vienetu nuo centro
  plane.rotation.y = Math.PI;     // nosis i skrydzio krypti
  plane.rotation.z = -0.25;       // pasviras posukyje
  var flying = true;
  var altitude = 0;

  // ---------- 1. INSTANCE: lankai (instance + instancedBuffers spalva) ----------
  var hoop = BABYLON.MeshBuilder.CreateTorus("hoop", { diameter: 4, thickness: 0.25, tessellation: 32 }, scene);
  var hoopMat = new BABYLON.StandardMaterial("hoopMat", scene);
  hoopMat.emissiveColor = new BABYLON.Color3(0.3, 0.3, 0.3);
  hoop.material = hoopMat;
  hoop.registerInstancedBuffer("color", 4);
  hoop.instancedBuffers.color = new BABYLON.Color4(1, 0.8, 0.1, 1);
  hoop.isVisible = false;         // pats saltinis nerodomas, rodomi tik instance
  var hoops = [];
  for (var i = 0; i < 6; i++) {
    var a = i * Math.PI * 2 / 6 + 0.5;
    var h = hoop.createInstance("hoop" + i);
    h.position = new BABYLON.Vector3(10 * Math.cos(a), 2, -10 * Math.sin(a));
    h.rotation = new BABYLON.Vector3(Math.PI / 2, a, 0);
    h.instancedBuffers.color = new BABYLON.Color4(1, 0.8, 0.1, 1);
    hoops.push(h);
  }

  // ---------- 1. INSTANCE: debesys ----------
  var cloudParts = [];
  [[0, 0, 0, 2], [1.4, -0.2, 0, 1.5], [-1.4, -0.3, 0.2, 1.4], [0.5, 0.6, 0.3, 1.3]].forEach(function (p) {
    var s = BABYLON.MeshBuilder.CreateSphere("cloudPart", { diameter: p[3], segments: 12 }, scene);
    s.position = new BABYLON.Vector3(p[0], p[1], p[2]);
    cloudParts.push(s);
  });
  var cloud = BABYLON.Mesh.MergeMeshes(cloudParts, true);
  cloud.name = "cloud";
  var cloudMat = new BABYLON.StandardMaterial("cloudMat", scene);
  cloudMat.diffuseColor = new BABYLON.Color3(1, 1, 1);
  cloudMat.emissiveColor = new BABYLON.Color3(0.5, 0.5, 0.55);
  cloud.material = cloudMat;
  cloud.position = new BABYLON.Vector3(-15, 9, 12);
  var clouds = [cloud];
  for (var c = 1; c < 8; c++) {
    var ci = cloud.createInstance("cloud" + c);
    var ca = c * Math.PI * 2 / 7;
    ci.position = new BABYLON.Vector3(22 * Math.cos(ca), 8 + Math.random() * 4, 8 + 18 * Math.sin(ca));
    ci.scaling.setAll(0.8 + Math.random() * 1.2);
    clouds.push(ci);
  }

  // ---------- 2. MESHAS IS BIBLIOTEKOS: kaiste (seagulf.glb) ant uolos ----------
  var rock = BABYLON.MeshBuilder.CreateIcoSphere("rock", { radius: 1.5, subdivisions: 2 }, scene);
  rock.position = new BABYLON.Vector3(-4, 0, 4);
  rock.scaling.y = 0.6;
  var rockMat = new BABYLON.StandardMaterial("rockMat", scene);
  rockMat.diffuseColor = new BABYLON.Color3(0.35, 0.32, 0.3);
  rock.material = rockMat;
  var gull = null;
  BABYLON.SceneLoader.ImportMeshAsync("", "https://assets.babylonjs.com/meshes/", "seagulf.glb", scene)
    .then(function (res) {
      gull = res.meshes[0];
      gull.scaling.scaleInPlace(0.0012);
      gull.position = new BABYLON.Vector3(-4, 0.8, 4);
      res.meshes.forEach(function (m) { addToScene(m); });
    });

  // ---------- 3. SESELIAI ----------
  light.position = new BABYLON.Vector3(20, 40, 20);
  var shadowGen = new BABYLON.ShadowGenerator(2048, light);
  shadowGen.useBlurExponentialShadowMap = true;
  shadowGen.blurKernel = 32;
  water.receiveShadows = true;

  // ---------- 3. VEIDRODINIS ATSPINDYS: vanduo ----------
  var mirror = new BABYLON.MirrorTexture("mirror", 1024, scene, true);
  mirror.mirrorPlane = new BABYLON.Plane(0, -1, 0, 0);
  mirror.level = 0.6;
  mirror.adaptiveBlurKernel = 16;  // vanduo atspindi ne idealiai
  blueMat.reflectionTexture = mirror;
  var skybox = scene.getMeshByName("BackgroundSkybox");
  if (skybox) mirror.renderList.push(skybox);

  function addToScene(m) {
    shadowGen.addShadowCaster(m);
    mirror.renderList.push(m);
  }
  plane.getChildMeshes().forEach(addToScene);
  hoops.forEach(addToScene);
  clouds.forEach(addToScene);
  addToScene(rock);

  // ---------- 5. DALELIU SISTEMA: dumai is duslintuvo ----------
  var smoke = new BABYLON.ParticleSystem("smoke", 2000, scene);
  smoke.particleTexture = new BABYLON.Texture("textures/cloud.png", scene);
  smoke.emitter = exhaust;                       // emiteris - lektuvo duslintuvas
  smoke.minEmitBox = new BABYLON.Vector3(-0.05, -0.05, 0);
  smoke.maxEmitBox = new BABYLON.Vector3(0.05, 0.05, 0);
  smoke.color1 = new BABYLON.Color4(0.8, 0.8, 0.8, 0.6);
  smoke.color2 = new BABYLON.Color4(0.6, 0.6, 0.65, 0.5);
  smoke.colorDead = new BABYLON.Color4(0.5, 0.5, 0.5, 0);
  smoke.minSize = 0.3; smoke.maxSize = 0.8;
  smoke.minScaleX = 1; smoke.maxScaleX = 1.5;
  smoke.minLifeTime = 0.8; smoke.maxLifeTime = 1.6;
  smoke.emitRate = 150;
  smoke.direction1 = new BABYLON.Vector3(-0.2, 0.3, -1);
  smoke.direction2 = new BABYLON.Vector3(0.2, 0.6, -1);
  smoke.minEmitPower = 0.5; smoke.maxEmitPower = 1;
  smoke.minAngularSpeed = -1; smoke.maxAngularSpeed = 1;
  smoke.addSizeGradient(0, 0.3);
  smoke.addSizeGradient(1, 1.5);
  smoke.blendMode = BABYLON.ParticleSystem.BLENDMODE_STANDARD;
  smoke.start();

  // ---------- 5. DALELIU SISTEMA: fejerverkas praskridus lanka ----------
  var burst = new BABYLON.ParticleSystem("burst", 1500, scene);
  burst.particleTexture = new BABYLON.Texture("textures/flare.png", scene);
  burst.emitter = new BABYLON.Vector3(0, 0, 0);
  burst.createSphereEmitter(1);
  burst.color1 = new BABYLON.Color4(1, 0.9, 0.2, 1);
  burst.color2 = new BABYLON.Color4(1, 0.3, 0.6, 1);
  burst.colorDead = new BABYLON.Color4(0, 0, 0, 0);
  burst.minSize = 0.2; burst.maxSize = 0.6;
  burst.minLifeTime = 0.5; burst.maxLifeTime = 1.2;
  burst.minEmitPower = 5; burst.maxEmitPower = 10;
  burst.gravity = new BABYLON.Vector3(0, -5, 0);
  burst.emitRate = 0;
  burst.blendMode = BABYLON.ParticleSystem.BLENDMODE_ONEONE;
  burst.start();
  function fireworks(pos) {
    burst.emitter = pos.clone();
    burst.manualEmitCount = 400;
  }

  // ---------- 7. SAVAS PAVYZDYS: Trail Mesh + Lens Flare ----------
  // Trail Mesh - balti pedsakai is sparnu galu
  var trailMat = new BABYLON.StandardMaterial("trailMat", scene);
  trailMat.emissiveColor = new BABYLON.Color3(1, 1, 1);
  trailMat.diffuseColor = BABYLON.Color3.Black();
  trailMat.alpha = 0.45;
  trailMat.backFaceCulling = false;
  [-3.5, 3.5].forEach(function (x) {
    var tip = new BABYLON.TransformNode("wingTip", scene);
    tip.parent = plane;
    tip.position = new BABYLON.Vector3(x, 0, -0.2);
    var trail = new BABYLON.TrailMesh("trail", tip, scene, 0.08, 120, true);
    trail.material = trailMat;
  });

  // Lens Flare - saule su objektyvo atspindziais
  var sun = BABYLON.MeshBuilder.CreateSphere("sun", { diameter: 4 }, scene);
  sun.position = light.direction.scale(-45);
  var sunMat = new BABYLON.StandardMaterial("sunMat", scene);
  sunMat.emissiveColor = new BABYLON.Color3(1, 0.95, 0.7);
  sunMat.disableLighting = true;
  sun.material = sunMat;
  var flares = new BABYLON.LensFlareSystem("flares", sun, scene);
  new BABYLON.LensFlare(0.3, 0, new BABYLON.Color3(1, 1, 1), "textures/flare.png", flares);
  new BABYLON.LensFlare(0.1, 0.4, new BABYLON.Color3(0.6, 0.8, 1), "textures/Flare2.png", flares);
  new BABYLON.LensFlare(0.08, 0.7, new BABYLON.Color3(1, 0.7, 0.4), "textures/flare3.png", flares);
  new BABYLON.LensFlare(0.15, 1.0, new BABYLON.Color3(0.7, 1, 0.7), "textures/flare3.png", flares);
  new BABYLON.LensFlare(0.05, -0.3, new BABYLON.Color3(1, 1, 1), "textures/Flare2.png", flares);

  // ---------- 6. IVYKIU TVARKYKLE: GUI ----------
  var score = 0;
  var ui = BABYLON.GUI.AdvancedDynamicTexture.CreateFullscreenUI("UI");
  var info = new BABYLON.GUI.TextBlock("info", "");
  info.color = "white"; info.fontSize = 16;
  info.outlineWidth = 3; info.outlineColor = "black";
  info.textHorizontalAlignment = BABYLON.GUI.Control.HORIZONTAL_ALIGNMENT_LEFT;
  info.textVerticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_TOP;
  info.paddingLeft = "12px"; info.paddingTop = "12px";
  ui.addControl(info);
  function updateInfo() {
    info.text = "Lankai: " + score +
      "\nW/S - aukstis, Space - stop/skristi, F - fejerverkas" +
      "\nPaspausk lektuva - kilpa";
  }
  updateInfo();

  var soundBtn = BABYLON.GUI.Button.CreateSimpleButton("soundBtn", "Garsas: ON");
  soundBtn.width = "140px"; soundBtn.height = "40px";
  soundBtn.color = "white"; soundBtn.background = "#1b4d6e";
  soundBtn.top = "-12px"; soundBtn.left = "-12px";
  soundBtn.horizontalAlignment = BABYLON.GUI.Control.HORIZONTAL_ALIGNMENT_RIGHT;
  soundBtn.verticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_BOTTOM;
  ui.addControl(soundBtn);
  soundBtn.onPointerUpObservable.add(function () {
    if (sound.isPlaying) { sound.pause(); soundBtn.textBlock.text = "Garsas: OFF"; }
    else { sound.play(); soundBtn.textBlock.text = "Garsas: ON"; }
  });

  // ---------- 6. IVYKIU TVARKYKLE: klaviatura ----------
  var keys = {};
  scene.onKeyboardObservable.add(function (kb) {
    var k = kb.event.key.toLowerCase();
    if (kb.type === BABYLON.KeyboardEventTypes.KEYDOWN) {
      keys[k] = true;
      if (k === " ") flying = !flying;
      if (k === "f") fireworks(plane.getAbsolutePosition().add(new BABYLON.Vector3(0, 3, 0)));
    } else if (kb.type === BABYLON.KeyboardEventTypes.KEYUP) {
      keys[k] = false;
    }
  });

  // ---------- 6. IVYKIU TVARKYKLE: ActionManager (pele ant lektuvo) ----------
  var looping = false;
  plane.getChildMeshes().forEach(function (m) {
    m.actionManager = new BABYLON.ActionManager(scene);
    m.actionManager.registerAction(new BABYLON.SetValueAction(
      BABYLON.ActionManager.OnPointerOverTrigger, redMat, "emissiveColor", new BABYLON.Color3(0.4, 0, 0)));
    m.actionManager.registerAction(new BABYLON.SetValueAction(
      BABYLON.ActionManager.OnPointerOutTrigger, redMat, "emissiveColor", BABYLON.Color3.Black()));
    m.actionManager.registerAction(new BABYLON.ExecuteCodeAction(
      BABYLON.ActionManager.OnPickTrigger, function () {
        if (looping) return;
        looping = true;
        var ease = new BABYLON.CubicEase();
        ease.setEasingMode(BABYLON.EasingFunction.EASINGMODE_EASEINOUT);
        BABYLON.Animation.CreateAndStartAnimation("barrelRoll", plane, "rotation.z", 30, 45,
          plane.rotation.z, plane.rotation.z + Math.PI * 2,
          BABYLON.Animation.ANIMATIONLOOPMODE_CONSTANT, ease, function () {
            plane.rotation.z = -0.25;
            looping = false;
          });
      }));
  });

  // ---------- 4. MESH INTERACTIONS + kadro logika ----------
  scene.onBeforeRenderObservable.add(function () {
    var dt = engine.getDeltaTime() / 1000;

    if (flying) flightPivot.rotation.y += dt * 0.5;   // pivoto sukimas = skrydis ratu
    if (gull) gull.rotate(BABYLON.Vector3.Up(), dt * 0.3);          // kaiste dairosi aplink
    clouds.forEach(function (c) { c.position.x += dt * 0.5; if (c.position.x > 30) c.position.x = -30; });

    if (keys["w"]) altitude = Math.min(altitude + dt * 3, 8);
    if (keys["s"]) altitude = Math.max(altitude - dt * 3, -1);
    flightPivot.position.y = altitude;

    smoke.emitRate = flying ? 150 : 20;

    // intersectsMesh: lektuvo korpusas praskrenda pro lanka
    hoops.forEach(function (h) {
      if (fuselage.intersectsMesh(h, false)) {
        if (!h.passed) {
          h.passed = true;
          score++;
          updateInfo();
          h.instancedBuffers.color = new BABYLON.Color4(0.2, 1, 0.3, 1);
          fireworks(h.getAbsolutePosition());
        }
      } else if (h.passed && BABYLON.Vector3.Distance(fuselage.getAbsolutePosition(), h.position) > 6) {
        h.passed = false;
        h.instancedBuffers.color = new BABYLON.Color4(1, 0.8, 0.1, 1);
      }
    });

    // intersectsMesh: lektuvas kliudo debesi - debesis susiploja
    clouds.forEach(function (c) {
      c.scaling.y += ((wings.intersectsMesh(c, false) ? 0.6 : 1) * c.scaling.x - c.scaling.y) * 0.1;
    });
  });
  // ANIMATION
  var time = 0;
  scene.registerBeforeRender(function () {
    var dt = engine.getDeltaTime() / 1000;
    time += dt;
    // TRANSLATION
    plane.position.y =
      2 + Math.sin(time * 0.6) * 0.15;
    // ROTATION
    propeller.rotation.z += dt * 18;
    // SCALING
    var scale =
      1 + Math.sin(time * 1.5) * 0.03;
    plane.scaling.x = scale;
    plane.scaling.y = scale;
    plane.scaling.z = scale;
  });
  return scene;
};
