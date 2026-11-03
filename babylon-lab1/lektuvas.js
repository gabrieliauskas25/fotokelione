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

  // ============ PAPILDYMAI ============
  camera.radius = 25;

  // 1. PIVOT - lektuvas skrenda ratu aplink centra
  var pivot = new BABYLON.TransformNode("pivot", scene);
  plane.parent = pivot;
  plane.position.x = 8;
  plane.rotation.y = Math.PI;

  // 1. INSTANCE - debesys
  var cloud = BABYLON.MeshBuilder.CreateSphere("cloud", { diameter: 3 }, scene);
  cloud.position = new BABYLON.Vector3(-10, 8, 10);
  var cloudMat = new BABYLON.StandardMaterial("cloudMat", scene);
  cloudMat.emissiveColor = new BABYLON.Color3(0.9, 0.9, 0.9);
  cloud.material = cloudMat;
  var cloud2 = cloud.createInstance("cloud2");
  cloud2.position = new BABYLON.Vector3(0, 9, 15);
  var cloud3 = cloud.createInstance("cloud3");
  cloud3.position = new BABYLON.Vector3(10, 8, 10);

  // 2. MESHAS IS BIBLIOTEKOS - kaiste
  BABYLON.SceneLoader.ImportMesh("", "https://assets.babylonjs.com/meshes/", "seagulf.glb", scene, function (meshes) {
    meshes[0].scaling.scaleInPlace(0.0015);
    meshes[0].position = new BABYLON.Vector3(-3, 0.8, 3);
    meshes.forEach(function (m) {
      shadowGen.addShadowCaster(m);
      mirror.renderList.push(m);
    });
  });

  // 3. SESELIAI
  light.position = new BABYLON.Vector3(20, 40, 20);
  var shadowGen = new BABYLON.ShadowGenerator(1024, light);
  plane.getChildMeshes().forEach(function (m) { shadowGen.addShadowCaster(m); });
  water.receiveShadows = true;

  // 3. VEIDRODINIS ATSPINDYS - vanduo
  var mirror = new BABYLON.MirrorTexture("mirror", 1024, scene, true);
  mirror.mirrorPlane = new BABYLON.Plane(0, -1, 0, 0);
  mirror.level = 0.6;
  mirror.renderList = plane.getChildMeshes().concat([cloud, cloud2, cloud3]);
  blueMat.reflectionTexture = mirror;

  // 4. INTERACTIONS - lankas, pro kuri praskrenda lektuvas
  var hoop = BABYLON.MeshBuilder.CreateTorus("hoop", { diameter: 4, thickness: 0.2 }, scene);
  hoop.position = new BABYLON.Vector3(0, 2, -8);
  hoop.rotation.x = Math.PI / 2;
  hoop.rotation.y = Math.PI / 2;
  var hoopMat = new BABYLON.StandardMaterial("hoopMat", scene);
  hoopMat.diffuseColor = BABYLON.Color3.Yellow();
  hoop.material = hoopMat;

  // 5. DALELIU SISTEMA - dumai is duslintuvo
  var smoke = new BABYLON.ParticleSystem("smoke", 1000, scene);
  smoke.particleTexture = new BABYLON.Texture("textures/flare.png", scene);
  smoke.emitter = exhaust;
  smoke.color1 = new BABYLON.Color4(0.8, 0.8, 0.8, 1);
  smoke.color2 = new BABYLON.Color4(0.5, 0.5, 0.5, 1);
  smoke.minSize = 0.2;
  smoke.maxSize = 0.5;
  smoke.minLifeTime = 0.5;
  smoke.maxLifeTime = 1.5;
  smoke.emitRate = 100;
  smoke.direction1 = new BABYLON.Vector3(0, 0.5, -1);
  smoke.direction2 = new BABYLON.Vector3(0, 1, -1);
  smoke.start();

  // 7. SAVAS PAVYZDYS - Trail Mesh (pedsakas uz lektuvo)
  var trail = new BABYLON.TrailMesh("trail", wings, scene, 0.2, 60, true);
  var trailMat = new BABYLON.StandardMaterial("trailMat", scene);
  trailMat.emissiveColor = BABYLON.Color3.White();
  trailMat.alpha = 0.5;
  trail.material = trailMat;

  // 6. IVYKIU TVARKYKLE - Space sustabdo / paleidzia lektuva
  var flying = true;
  scene.onKeyboardObservable.add(function (kb) {
    if (kb.type === BABYLON.KeyboardEventTypes.KEYDOWN && kb.event.key === " ") {
      flying = !flying;
    }
  });

  scene.onBeforeRenderObservable.add(function () {
    if (flying) pivot.rotation.y += 0.01;

    // 4. jei lektuvas liecia lanka - lankas pazaliuoja
    if (fuselage.intersectsMesh(hoop, false)) {
      hoopMat.diffuseColor = BABYLON.Color3.Green();
    } else {
      hoopMat.diffuseColor = BABYLON.Color3.Yellow();
    }
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
