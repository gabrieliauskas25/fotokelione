export const createScene = function () {
  var scene = new BABYLON.Scene(engine);
  scene.clearColor = new BABYLON.Color4(0.68, 0.85, 0.95, 1);

  // HELPER
  scene.createDefaultEnvironment({ createGround: false, createSkybox: true, skyboxSize: 100 });

  // CAMERA
  var camera = new BABYLON.ArcRotateCamera("camera", -Math.PI / 2, Math.PI / 2.5, 25, new BABYLON.Vector3(0, 1, 0), scene);
  camera.attachControl(canvas, true);

  // SUN LIGHT
  var light = new BABYLON.DirectionalLight("sunLight", new BABYLON.Vector3(-1, -2, -1), scene);
  light.position = new BABYLON.Vector3(20, 40, 20);
  light.intensity = 1;
  var hemi = new BABYLON.HemisphericLight("hemi", new BABYLON.Vector3(0, 1, 0), scene);
  hemi.intensity = 0.4;

  // SOUND
  var sound = new BABYLON.Sound("sound", "sounds/violons11.wav", scene, null, { loop: true, autoplay: true, volume: 0.15 });

  // MATERIALS
  var redMat = new BABYLON.StandardMaterial("redMat", scene);
  redMat.diffuseColor = new BABYLON.Color3(0.85, 0.15, 0.18);
  var whiteMat = new BABYLON.StandardMaterial("whiteMat", scene);
  whiteMat.diffuseColor = new BABYLON.Color3(0.92, 0.92, 0.95);
  var darkMat = new BABYLON.StandardMaterial("darkMat", scene);
  darkMat.diffuseColor = new BABYLON.Color3(0.08, 0.08, 0.1);
  var blueMat = new BABYLON.StandardMaterial("blueMat", scene);
  blueMat.diffuseColor = new BABYLON.Color3(0.05, 0.35, 0.55);
  var glassMat = new BABYLON.StandardMaterial("glassMat", scene);
  glassMat.diffuseColor = new BABYLON.Color3(0.15, 0.3, 0.4);
  glassMat.alpha = 0.7;

  // TEXTURE
  blueMat.bumpTexture = new BABYLON.Texture("https://assets.babylonjs.com/textures/waterbump.png", scene);

  // ===== 1. PIVOT: lektuvas skrenda ratu aplink pivota =====
  var pivot = new BABYLON.TransformNode("pivot", scene);
  var plane = new BABYLON.TransformNode("plane", scene);
  plane.parent = pivot;
  plane.position = new BABYLON.Vector3(8, 2, 0);
  plane.rotation.y = Math.PI;

  // AIRPLANE PARTS
  var fuselage = BABYLON.MeshBuilder.CreateCylinder("fuselage", { diameterTop: 0.6, diameterBottom: 0.9, height: 5, tessellation: 16 }, scene);
  fuselage.rotation.x = Math.PI / 2;
  fuselage.material = redMat;
  fuselage.parent = plane;

  var wings = BABYLON.MeshBuilder.CreateBox("wings", { width: 7, height: 0.15, depth: 1.2 }, scene);
  wings.position.z = -0.2;
  wings.material = whiteMat;
  wings.parent = plane;

  var cockpit = BABYLON.MeshBuilder.CreateSphere("cockpit", { diameter: 0.5 }, scene);
  cockpit.position = new BABYLON.Vector3(0, 0.45, 0.7);
  cockpit.scaling.z = 1.5;
  cockpit.material = glassMat;
  cockpit.parent = plane;

  var ring = BABYLON.MeshBuilder.CreateTorus("ring", { diameter: 0.45, thickness: 0.2 }, scene);
  ring.rotation.x = Math.PI / 2;
  ring.position.z = 2.5;
  ring.material = darkMat;
  ring.parent = plane;

  var capsule = BABYLON.MeshBuilder.CreateCapsule("capsule", { height: 1.0, radius: 0.25 }, scene);
  capsule.rotation.z = Math.PI / 2;
  capsule.position = new BABYLON.Vector3(0, -0.3, 0.5);
  capsule.material = darkMat;
  capsule.parent = plane;

  var ico = BABYLON.MeshBuilder.CreateIcoSphere("ico", { radius: 0.3 }, scene);
  ico.position.y = 0.35;
  ico.material = darkMat;
  ico.parent = plane;

  var verticalTail = BABYLON.MeshBuilder.CreatePlane("verticalTail", { width: 1.2, height: 1.4, sideOrientation: BABYLON.Mesh.DOUBLESIDE }, scene);
  verticalTail.position = new BABYLON.Vector3(0, 0.65, -2.35);
  verticalTail.rotation.y = Math.PI / 2;
  verticalTail.material = redMat;
  verticalTail.parent = plane;

  var horizontalTail = BABYLON.MeshBuilder.CreateBox("horizontalTail", { width: 2.5, height: 0.12, depth: 0.6 }, scene);
  horizontalTail.position = new BABYLON.Vector3(0, 0.15, -2.35);
  horizontalTail.material = whiteMat;
  horizontalTail.parent = plane;

  var exhaust = BABYLON.MeshBuilder.CreateTube("exhaust", { path: [new BABYLON.Vector3(0, 0, 0), new BABYLON.Vector3(0, 0, 0.5)], radius: 0.15, tessellation: 16 }, scene);
  exhaust.position.z = -2.5;
  exhaust.material = darkMat;
  exhaust.parent = plane;

  var lathe = BABYLON.MeshBuilder.CreateLathe("lathe", {
    shape: [new BABYLON.Vector3(0, 0, 0), new BABYLON.Vector3(0.12, 0, 0.1), new BABYLON.Vector3(0.18, 0, 0.3),
            new BABYLON.Vector3(0.12, 0, 0.5), new BABYLON.Vector3(0, 0, 0.5)],
    radius: 1, tessellation: 12 }, scene);
  lathe.scaling = new BABYLON.Vector3(0.6, 0.6, 0.6);
  lathe.position = new BABYLON.Vector3(0, 0.1, -2.55);
  lathe.material = darkMat;
  lathe.parent = plane;

  // PROPELLER (pivotas propeleriui)
  var propeller = new BABYLON.TransformNode("propeller", scene);
  propeller.position.z = 2.5;
  propeller.parent = plane;
  var hub = BABYLON.MeshBuilder.CreateSphere("hub", { diameter: 0.35 }, scene);
  hub.material = darkMat;
  hub.parent = propeller;
  var blade1 = BABYLON.MeshBuilder.CreateBox("blade1", { width: 0.12, height: 1.4, depth: 0.03 }, scene);
  blade1.material = darkMat;
  blade1.parent = propeller;
  var blade2 = blade1.createInstance("blade2");   // INSTANCE
  blade2.rotation.z = Math.PI / 2;
  blade2.parent = propeller;

  // WHEELS + STRUTS (antri yra INSTANCE)
  var wheel1 = BABYLON.MeshBuilder.CreateCylinder("wheel1", { diameter: 0.4, height: 0.18 }, scene);
  wheel1.rotation.z = Math.PI / 2;
  wheel1.position = new BABYLON.Vector3(-1.6, -0.7, -0.2);
  wheel1.material = darkMat;
  wheel1.parent = plane;
  var wheel2 = wheel1.createInstance("wheel2");
  wheel2.position.x = 1.6;
  wheel2.parent = plane;

  var strut1 = BABYLON.MeshBuilder.CreateCylinder("strut1", { diameter: 0.08, height: 0.55 }, scene);
  strut1.position = new BABYLON.Vector3(-1.6, -0.38, -0.2);
  strut1.material = darkMat;
  strut1.parent = plane;
  var strut2 = strut1.createInstance("strut2");
  strut2.position.x = 1.6;
  strut2.parent = plane;

  // GROUND - WATER
  var water = BABYLON.MeshBuilder.CreateGround("water", { width: 60, height: 60, subdivisions: 40 }, scene);
  water.material = blueMat;

  // ===== 1. INSTANCE: debesys =====
  var cloudMat = new BABYLON.StandardMaterial("cloudMat", scene);
  cloudMat.emissiveColor = new BABYLON.Color3(0.9, 0.9, 0.9);
  var cloud = BABYLON.MeshBuilder.CreateSphere("cloud", { diameter: 3 }, scene);
  cloud.position = new BABYLON.Vector3(-10, 8, 10);
  cloud.scaling.x = 2;
  cloud.material = cloudMat;
  var cloud2 = cloud.createInstance("cloud2");
  cloud2.position = new BABYLON.Vector3(0, 10, 15);
  var cloud3 = cloud.createInstance("cloud3");
  cloud3.position = new BABYLON.Vector3(10, 8, 10);

  // ===== 2. MESHAS IS BIBLIOTEKOS: kaiste ant uolos =====
  var rock = BABYLON.MeshBuilder.CreateIcoSphere("rock", { radius: 1.5, subdivisions: 2 }, scene);
  rock.position = new BABYLON.Vector3(-3, 0, 3);
  rock.scaling.y = 0.6;
  rock.material = darkMat;
  BABYLON.SceneLoader.ImportMesh("", "https://assets.babylonjs.com/meshes/", "seagulf.glb", scene, function (meshes) {
    meshes[0].scaling.scaleInPlace(0.0015);
    meshes[0].position = new BABYLON.Vector3(-3, 1.6, 3);
    meshes.forEach(function (m) {
      shadowGen.addShadowCaster(m);
      mirror.renderList.push(m);
    });
  });

  // ===== 3. SESELIAI =====
  var shadowGen = new BABYLON.ShadowGenerator(1024, light);
  shadowGen.useBlurExponentialShadowMap = true;
  plane.getChildMeshes().forEach(function (m) { shadowGen.addShadowCaster(m); });
  shadowGen.addShadowCaster(rock);
  water.receiveShadows = true;

  // ===== 3. VEIDRODINIS ATSPINDYS: vanduo =====
  var mirror = new BABYLON.MirrorTexture("mirror", 1024, scene, true);
  mirror.mirrorPlane = new BABYLON.Plane(0, -1, 0, 0);
  mirror.level = 0.6;
  mirror.renderList = plane.getChildMeshes().concat([cloud, cloud2, cloud3, rock]);
  mirror.renderParticles = true;
  blueMat.reflectionTexture = mirror;

  // ===== 4. INTERACTIONS: lankas, pro kuri skrenda lektuvas =====
  var hoop = BABYLON.MeshBuilder.CreateTorus("hoop", { diameter: 4, thickness: 0.2, tessellation: 32 }, scene);
  hoop.position = new BABYLON.Vector3(0, 2, -8);
  hoop.rotation = new BABYLON.Vector3(Math.PI / 2, Math.PI / 2, 0);
  var hoopMat = new BABYLON.StandardMaterial("hoopMat", scene);
  hoopMat.emissiveColor = BABYLON.Color3.Yellow();
  hoop.material = hoopMat;
  mirror.renderList.push(hoop);
  var score = 0;
  var wasInside = false;

  // ===== 5. DALELIU SISTEMA 1: dumai is duslintuvo =====
  var smoke = new BABYLON.ParticleSystem("smoke", 1000, scene);
  smoke.particleTexture = new BABYLON.Texture("textures/flare.png", scene);
  smoke.emitter = exhaust;
  smoke.color1 = new BABYLON.Color4(0.8, 0.8, 0.8, 1);
  smoke.color2 = new BABYLON.Color4(0.5, 0.5, 0.5, 1);
  smoke.colorDead = new BABYLON.Color4(0.3, 0.3, 0.3, 0);
  smoke.minSize = 0.2;
  smoke.maxSize = 0.5;
  smoke.minLifeTime = 0.5;
  smoke.maxLifeTime = 1.5;
  smoke.emitRate = 100;
  smoke.direction1 = new BABYLON.Vector3(0, 0.5, -1);
  smoke.direction2 = new BABYLON.Vector3(0, 1, -1);
  smoke.start();

  // ===== 5. DALELIU SISTEMA 2: fejerverkas (kai praskrenda lanka) =====
  var fireworks = new BABYLON.ParticleSystem("fireworks", 1000, scene);
  fireworks.particleTexture = new BABYLON.Texture("textures/flare.png", scene);
  fireworks.emitter = hoop;
  fireworks.createSphereEmitter(1);
  fireworks.color1 = new BABYLON.Color4(1, 0.8, 0, 1);
  fireworks.color2 = new BABYLON.Color4(1, 0.2, 0.5, 1);
  fireworks.colorDead = new BABYLON.Color4(0, 0, 0, 0);
  fireworks.minSize = 0.2;
  fireworks.maxSize = 0.5;
  fireworks.minLifeTime = 0.5;
  fireworks.maxLifeTime = 1;
  fireworks.minEmitPower = 5;
  fireworks.maxEmitPower = 8;
  fireworks.emitRate = 0;          // daleles isleidziamos tik per manualEmitCount
  fireworks.start();

  // ===== 7. SAVAS PAVYZDYS: Trail Mesh + Lens Flare =====
  var trailMat = new BABYLON.StandardMaterial("trailMat", scene);
  trailMat.emissiveColor = BABYLON.Color3.White();
  trailMat.alpha = 0.5;
  var trail = new BABYLON.TrailMesh("trail", wings, scene, 0.2, 60, true);
  trail.material = trailMat;

  var sun = BABYLON.MeshBuilder.CreateSphere("sun", { diameter: 4 }, scene);
  sun.position = new BABYLON.Vector3(-20, 15, 40);
  var sunMat = new BABYLON.StandardMaterial("sunMat", scene);
  sunMat.emissiveColor = new BABYLON.Color3(1, 0.9, 0.6);
  sun.material = sunMat;
  var flares = new BABYLON.LensFlareSystem("flares", sun, scene);
  new BABYLON.LensFlare(0.3, 0, new BABYLON.Color3(1, 1, 1), "textures/flare.png", flares);
  new BABYLON.LensFlare(0.1, 0.4, new BABYLON.Color3(0.6, 0.8, 1), "textures/Flare2.png", flares);
  new BABYLON.LensFlare(0.15, 0.8, new BABYLON.Color3(1, 0.7, 0.4), "textures/flare3.png", flares);

  // ===== 6. IVYKIU TVARKYKLE =====
  // GUI tekstas
  var ui = BABYLON.GUI.AdvancedDynamicTexture.CreateFullscreenUI("UI");
  var info = new BABYLON.GUI.TextBlock("info", "Lankai: 0\nSpace - stop, W/S - aukstis, paspausk lektuva");
  info.color = "black";
  info.fontSize = 18;
  info.textVerticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_TOP;
  info.textHorizontalAlignment = BABYLON.GUI.Control.HORIZONTAL_ALIGNMENT_LEFT;
  info.paddingLeft = "10px";
  info.paddingTop = "10px";
  ui.addControl(info);

  // Klaviatura: Space - stop/skristi, W/S - aukstis
  var flying = true;
  scene.onKeyboardObservable.add(function (kb) {
    if (kb.type !== BABYLON.KeyboardEventTypes.KEYDOWN) return;
    if (kb.event.key === " ") flying = !flying;
    if (kb.event.key === "w") pivot.position.y += 0.5;
    if (kb.event.key === "s") pivot.position.y -= 0.5;
  });

  // Pele: paspaudus lektuva - jis apsisuka per sparna (kilpa)
  fuselage.actionManager = new BABYLON.ActionManager(scene);
  fuselage.actionManager.registerAction(new BABYLON.ExecuteCodeAction(BABYLON.ActionManager.OnPickTrigger, function () {
    BABYLON.Animation.CreateAndStartAnimation("roll", plane, "rotation.z", 30, 30, 0, Math.PI * 2, 0);
  }));

  // ===== ANIMATION (kiekvienas kadras) =====
  var time = 0;
  scene.onBeforeRenderObservable.add(function () {
    var dt = engine.getDeltaTime() / 1000;
    time += dt;

    if (flying) pivot.rotation.y += dt * 0.6;                 // skrydis ratu
    plane.position.y = 2 + Math.sin(time * 0.6) * 0.15;      // TRANSLATION
    propeller.rotation.z += dt * 18;                          // ROTATION
    var scale = 1 + Math.sin(time * 1.5) * 0.03;              // SCALING
    plane.scaling = new BABYLON.Vector3(scale, scale, scale);

    // 4. intersectsMesh: lektuvas praskrenda pro lanka
    var inside = fuselage.intersectsMesh(hoop, false);
    if (inside && !wasInside) {
      score++;
      info.text = "Lankai: " + score + "\nSpace - stop, W/S - aukstis, paspausk lektuva";
      fireworks.manualEmitCount = 300;
    }
    hoopMat.emissiveColor = inside ? BABYLON.Color3.Green() : BABYLON.Color3.Yellow();
    wasInside = inside;
  });

  return scene;
};
