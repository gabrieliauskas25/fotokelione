export const createScene = function () {
  // ==================== ANTRO DARBO KODAS ====================
  var scene = new BABYLON.Scene(engine);
  scene.clearColor = new BABYLON.Color4(0.68, 0.85, 0.95, 1);
  scene.createDefaultEnvironment({ createGround: false, createSkybox: true, skyboxSize: 100 });

  // kamera
  var camera = new BABYLON.ArcRotateCamera("camera", -Math.PI / 2, Math.PI / 2.5, 15, new BABYLON.Vector3(0, 1, 0), scene);
  camera.attachControl(canvas, true);

  // saule
  var light = new BABYLON.DirectionalLight("sunLight", new BABYLON.Vector3(-1, -2, -1), scene);
  light.intensity = 1;

  // garsas
  var sound = new BABYLON.Sound("sound", "sounds/violons11.wav", scene, null, { loop: true, autoplay: true, volume: 0.15 });

  // medziagos
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

  // vandens tekstura
  blueMat.bumpTexture = new BABYLON.Texture("https://assets.babylonjs.com/textures/waterbump.png", scene);

  // lektuvas
  var plane = new BABYLON.TransformNode("plane", scene);
  plane.position.y = 2;
  // korpusas
  var fuselage = BABYLON.MeshBuilder.CreateCylinder("fuselage", { diameterTop: 0.6, diameterBottom: 0.9, height: 5, tessellation: 16 }, scene);
  fuselage.rotation.x = Math.PI / 2;
  fuselage.material = redMat;
  fuselage.parent = plane;
  // sparnai
  var wings = BABYLON.MeshBuilder.CreateBox("wings", { width: 7, height: 0.15, depth: 1.2 }, scene);
  wings.position.z = -0.2;
  wings.material = whiteMat;
  wings.parent = plane;
  // kabina
  var cockpit = BABYLON.MeshBuilder.CreateSphere("cockpit", { diameter: 0.5 }, scene);
  cockpit.position = new BABYLON.Vector3(0, 0.45, 0.7);
  cockpit.scaling.z = 1.5;
  cockpit.material = glassMat;
  cockpit.parent = plane;
  // priekinis ziedas
  var ring = BABYLON.MeshBuilder.CreateTorus("ring", { diameter: 0.45, thickness: 0.2 }, scene);
  ring.rotation.x = Math.PI / 2;
  ring.position.z = 2.5;
  ring.material = darkMat;
  ring.parent = plane;
  // kapsule apacioj
  var capsule = BABYLON.MeshBuilder.CreateCapsule("capsule", { height: 1.0, radius: 0.25 }, scene);
  capsule.rotation.z = Math.PI / 2;
  capsule.position = new BABYLON.Vector3(0, -0.3, 0.5);
  capsule.material = darkMat;
  capsule.parent = plane;
  // detale virsuj
  var ico = BABYLON.MeshBuilder.CreateIcoSphere("ico", { radius: 0.3 }, scene);
  ico.position.y = 0.35;
  ico.material = darkMat;
  ico.parent = plane;
  // vertikalus uodegos sparnas
  var verticalTail = BABYLON.MeshBuilder.CreatePlane("verticalTail", { width: 1.2, height: 1.4, sideOrientation: BABYLON.Mesh.DOUBLESIDE }, scene);
  verticalTail.position = new BABYLON.Vector3(0, 0.65, -2.35);
  verticalTail.rotation.y = Math.PI / 2;
  verticalTail.material = redMat;
  verticalTail.parent = plane;
  // horizontalus uodegos sparnas
  var horizontalTail = BABYLON.MeshBuilder.CreateBox("horizontalTail", { width: 2.5, height: 0.12, depth: 0.6 }, scene);
  horizontalTail.position = new BABYLON.Vector3(0, 0.15, -2.35);
  horizontalTail.material = whiteMat;
  horizontalTail.parent = plane;
  // duslintuvas
  var exhaust = BABYLON.MeshBuilder.CreateTube("exhaust", { path: [new BABYLON.Vector3(0, 0, 0), new BABYLON.Vector3(0, 0, 0.5)], radius: 0.15, tessellation: 16 }, scene);
  exhaust.position.z = -2.5;
  exhaust.material = darkMat;
  exhaust.parent = plane;
  // galine detale
  var lathe = BABYLON.MeshBuilder.CreateLathe("lathe", {
    shape: [new BABYLON.Vector3(0, 0, 0), new BABYLON.Vector3(0.12, 0, 0.1), new BABYLON.Vector3(0.18, 0, 0.3),
            new BABYLON.Vector3(0.12, 0, 0.5), new BABYLON.Vector3(0, 0, 0.5)],
    radius: 1, tessellation: 12 }, scene);
  lathe.scaling = new BABYLON.Vector3(0.6, 0.6, 0.6);
  lathe.position = new BABYLON.Vector3(0, 0.1, -2.55);
  lathe.material = darkMat;
  lathe.parent = plane;
  // propeleris
  var propeller = new BABYLON.TransformNode("propeller", scene);
  propeller.position.z = 2.5;
  propeller.parent = plane;
  var hub = BABYLON.MeshBuilder.CreateSphere("hub", { diameter: 0.35 }, scene);
  hub.material = darkMat;
  hub.parent = propeller;
  var blade1 = BABYLON.MeshBuilder.CreateBox("blade1", { width: 0.12, height: 1.4, depth: 0.03 }, scene);
  blade1.material = darkMat;
  blade1.parent = propeller;
  var blade2 = blade1.clone("blade2");
  blade2.rotation.z = Math.PI / 2;
  blade2.parent = propeller;
  // ratai
  var wheel1 = BABYLON.MeshBuilder.CreateCylinder("wheel1", { diameter: 0.4, height: 0.18 }, scene);
  wheel1.rotation.z = Math.PI / 2;
  wheel1.position = new BABYLON.Vector3(-1.6, -0.7, -0.2);
  wheel1.material = darkMat;
  wheel1.parent = plane;
  var wheel2 = wheel1.clone("wheel2");
  wheel2.position.x = 1.6;
  wheel2.parent = plane;
  // ratu atramos
  var strut1 = BABYLON.MeshBuilder.CreateCylinder("strut1", { diameter: 0.08, height: 0.55 }, scene);
  strut1.position = new BABYLON.Vector3(-1.6, -0.38, -0.2);
  strut1.material = darkMat;
  strut1.parent = plane;
  var strut2 = strut1.clone("strut2");
  strut2.position.x = 1.6;
  strut2.parent = plane;

  // vanduo
  var water = BABYLON.MeshBuilder.CreateGround("water", { width: 60, height: 60, subdivisions: 40 }, scene);
  water.material = blueMat;

  // animacija
  var time = 0;
  scene.registerBeforeRender(function () {
    var dt = engine.getDeltaTime() / 1000;
    time += dt;
    plane.position.y = 2 + Math.sin(time * 0.6) * 0.15;   // kyla ir leidziasi
    propeller.rotation.z += dt * 18;                       // sukasi propeleris
    var scale = 1 + Math.sin(time * 1.5) * 0.03;           // truputi dideja/mazeja
    plane.scaling = new BABYLON.Vector3(scale, scale, scale);
  });

  // ==================== PIRMO DARBO KODAS ====================
  camera.radius = 25;
  light.position = new BABYLON.Vector3(20, 40, 20);
  var hemi = new BABYLON.HemisphericLight("hemi", new BABYLON.Vector3(0, 1, 0), scene);
  hemi.intensity = 0.4;

  // pivot - lektuvas skraido ratu aplink centra
  var pivot = new BABYLON.TransformNode("pivot", scene);
  plane.parent = pivot;
  plane.position.x = 8;
  plane.rotation.y = Math.PI;

  // instance - debesys (vienas tikras, kiti du kopijos)
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

  // meshas is bibliotekos - kaiste ant akmens
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

  // seseliai
  var shadowGen = new BABYLON.ShadowGenerator(1024, light);
  shadowGen.useBlurExponentialShadowMap = true;
  plane.getChildMeshes().forEach(function (m) { shadowGen.addShadowCaster(m); });
  shadowGen.addShadowCaster(rock);
  water.receiveShadows = true;

  // veidrodis - vanduo atspindi objektus
  var mirror = new BABYLON.MirrorTexture("mirror", 1024, scene, true);
  mirror.mirrorPlane = new BABYLON.Plane(0, -1, 0, 0);
  mirror.level = 0.6;
  mirror.renderList = plane.getChildMeshes().concat([cloud, cloud2, cloud3, rock]);
  mirror.renderParticles = true;
  blueMat.reflectionTexture = mirror;

  // lankas pro kuri praskrenda lektuvas
  var hoop = BABYLON.MeshBuilder.CreateTorus("hoop", { diameter: 4, thickness: 0.2, tessellation: 32 }, scene);
  hoop.position = new BABYLON.Vector3(0, 2, -8);
  hoop.rotation = new BABYLON.Vector3(Math.PI / 2, Math.PI / 2, 0);
  var hoopMat = new BABYLON.StandardMaterial("hoopMat", scene);
  hoopMat.emissiveColor = BABYLON.Color3.Yellow();
  hoop.material = hoopMat;
  mirror.renderList.push(hoop);

  // daleles - fejerverkas prie lanko
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
  fireworks.emitRate = 0;   // pats nesprogsta, tik kai praskrenda
  fireworks.start();

  // savas pavyzdys - balta juosta uz sparnu (trail mesh)
  var trailMat = new BABYLON.StandardMaterial("trailMat", scene);
  trailMat.emissiveColor = BABYLON.Color3.White();
  trailMat.alpha = 0.5;
  var trail = new BABYLON.TrailMesh("trail", wings, scene, 0.2, 60, true);
  trail.material = trailMat;

  // savas pavyzdys - saule su blyksniais (lens flare)
  var sun = BABYLON.MeshBuilder.CreateSphere("sun", { diameter: 4 }, scene);
  sun.position = new BABYLON.Vector3(-20, 15, 40);
  var sunMat = new BABYLON.StandardMaterial("sunMat", scene);
  sunMat.emissiveColor = new BABYLON.Color3(1, 0.9, 0.6);
  sun.material = sunMat;
  var flares = new BABYLON.LensFlareSystem("flares", sun, scene);
  new BABYLON.LensFlare(0.3, 0, new BABYLON.Color3(1, 1, 1), "textures/flare.png", flares);
  new BABYLON.LensFlare(0.1, 0.4, new BABYLON.Color3(0.6, 0.8, 1), "textures/Flare2.png", flares);
  new BABYLON.LensFlare(0.15, 0.8, new BABYLON.Color3(1, 0.7, 0.4), "textures/flare3.png", flares);

  // tekstas ekrane
  var score = 0;
  var ui = BABYLON.GUI.AdvancedDynamicTexture.CreateFullscreenUI("UI");
  var info = new BABYLON.GUI.TextBlock("info", "Lankai: 0\nSpace - stop, W/S - aukstis, paspausk lektuva");
  info.color = "black";
  info.fontSize = 18;
  info.textVerticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_TOP;
  info.textHorizontalAlignment = BABYLON.GUI.Control.HORIZONTAL_ALIGNMENT_LEFT;
  info.paddingLeft = "10px";
  info.paddingTop = "10px";
  ui.addControl(info);

  // klaviatura - space sustabdo, w/s keicia auksti
  var flying = true;
  scene.onKeyboardObservable.add(function (kb) {
    if (kb.type !== BABYLON.KeyboardEventTypes.KEYDOWN) return;
    if (kb.event.key === " ") flying = !flying;
    if (kb.event.key === "w") pivot.position.y += 0.5;
    if (kb.event.key === "s") pivot.position.y -= 0.5;
  });

  // paspaudus ant lektuvo jis apsivercia
  fuselage.actionManager = new BABYLON.ActionManager(scene);
  fuselage.actionManager.registerAction(new BABYLON.ExecuteCodeAction(BABYLON.ActionManager.OnPickTrigger, function () {
    BABYLON.Animation.CreateAndStartAnimation("roll", plane, "rotation.z", 30, 30, 0, Math.PI * 2, 0);
  }));

  // kas kadra
  var wasInside = false;
  scene.onBeforeRenderObservable.add(function () {
    if (flying) pivot.rotation.y += 0.01;   // sukam pivota

    // ar lektuvas kerta lanka
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
