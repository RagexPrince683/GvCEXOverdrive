//function renderFirst(renderinstance,gunitem,gunstack,model,entity,type,data){
////var minecraft = renderinstance.getminecraft();//�}�C���N���t�g�̃C���X�^���X�B�����g��Ȃ�
////model.renderAll();
////org.lwjgl.opengl.GL11.glRotatef(180, 1.0, 0.0, 0.0);
////org.lwjgl.opengl.GL11.glRotatef(50, 0.0, 1.0, 0.0);
////org.lwjgl.opengl.GL11.glRotatef(180, 0.0, 0.0, 1.0);
////var cocking = renderinstance.getbooleanfromnbt("Cocking");//�R�b�L���O���Ă��邩�ǂ���
////var cockingprogress = renderinstance.getintfromnbt("CockingTime") + renderinstance.getSmoothing() - 1;//�R�b�L���O�J�n����̎��ԁifloat�l�j
//var recoiled = renderinstance.getbooleanfromnbt("Recoiled");//���R�C�������ǂ����i�e������u���˂�l�ɕ`�悷�邽�߂̃t���O�j
////var recoiledtime = renderinstance.getintfromnbt("RecoiledTime");//�g�r�s��
////var boltprogress = renderinstance.getbytefromnbt("Bolt");//�{���g�A�R�b�L���O���Ȃ�AR�E�Z�~SR�n�ŋ��e�̃A�j���[�V�������������߁Bbyte�l(int�l�Ɉ�����͂�)
//var isreloading = renderinstance.getbooleanfromnbt("IsReloading")//�����[�h�����ǂ���
////var Crotex = renderinstance.getfloatfromnbt("rotex");//�V�����_�[�̉�]X
////var Crotey = renderinstance.getfloatfromnbt("rotey");//�V�����_�[�̉�]Y
////var Crotez = renderinstance.getfloatfromnbt("rotez");//�V�����_�[�̉�]Z
////boltprogress -= renderinstance.getSmoothing();
////var cycle = gunitem.cycle;//�e�̋��e�ɂ����鎞�ԁifor AR/semiSR�j
////var boltoffsetcof;//�Ȟ��̉������Č����邽�߂̕��Bboltoffsetcof��0-1�𓮂��܂��B����ň�u�X���C�h���߂�l�q��`��o���܂��B
////if (boltprogress < cycle / 2)//�{���g��ޒ�
////	boltoffsetcof = cycle - boltprogress;
////else
////	boltoffsetcof = cycle - (cycle - boltprogress);
////if (boltoffsetcof < 0) boltoffsetcof = 0;
//org.lwjgl.opengl.GL11.glPushMatrix();//�J�n���ɂ�����Ă�ōs����o�b�N�A�b�v���܂��B
////renderinstance.bindGuntexture()//�e�̃e�N�X�`�����g�p���邽�߂̃��\�b�h�B�X�N���v�g�`��ɓ���O�Ɋ��蓖�ĂĂ���̂Œʏ�͎g�����Ƃ͖������Ǝv���܂��B
////glTranslatef�ňړ��A�`�恨�p�[�c���ړ�����ĕ`�悳���B
////glTranslatef�ňړ��AglRotatef�ŉ�]���ړ���ŉ�]����ĕ`�悳���B
////glTranslatef�ňړ��AglRotatef�ŉ�]�AglTranslatef�ōŏ��Ɠ����ʋt�����Ɉړ�������_�𒆐S�ɉ�]�������ƂɂȂ�B
//if (Packages.handmadeguns.mod_HandmadeGuns.Key_ADS(entity)){
//    //ADS���ɌĂ΂�镔��
//	if (isreloading){
//	    //ADS�������[�h���ɌĂ΂�镔��
//	    var reloadprogress = renderinstance.getintfromnbt("RloadTime");
//		var sighttype = renderinstance.bindAttach_SightPosition_and_getSightType(entity,gunstack);//�T�C�g��`�����߂Ɉʒu�𒲐����A�����Ɏ�ނ��擾
//		renderinstance.glMatrixForRenderInEquipped_reload();//�e�ɐݒ肳��Ă���`��ʒu��K�p
//        renderpartsOnReload(renderinstance,gunitem,gunstack,model,entity,type,data);
//	} else {
//	    if (!recoiled) {
//        	renderinstance.glMatrixForRenderInEquippedADS(-1.4 - 0.03 * (1 - renderinstance.getSmoothing()));
//        } else {
//        	renderinstance.glMatrixForRenderInEquippedADS(-1.4);
//        }
//        renderpartsNormal(renderinstance,gunitem,gunstack,model,entity,type,data);
//	}
//
//} else {
//	if (isreloading) {
//	    //�����[�h���ɌĂ΂�܂��B
//	    //�����[�h�J�n����̎��Ԃ�RloadTime�Ŏ��܂��B
//		renderinstance.glMatrixForRenderInEquipped_reload();//�`��n�_�������[�h���ʒu�ցBglTranslatef�ňړ����Ă����Ȃ��B
//		renderpartsOnReload(renderinstance,gunitem,gunstack,model,entity,type,data);
//	} else {
//		var sighttype = renderinstance.bindAttach_SightPosition_and_getSightType(entity,gunstack);//�T�C�g��`�����߂Ɉʒu�𒲐����A�����Ɏ�ނ��擾
//	    if (renderinstance.isentitysprinting(entity) && !renderinstance.getbooleanfromnbt("isBursting")) {
//    		renderinstance.glMatrixForRenderInEquipped(0);
//    		org.lwjgl.opengl.GL11.glRotatef(gunitem.Sprintrotationx, 1.0, 0.0, 0.0);
//    		org.lwjgl.opengl.GL11.glRotatef(gunitem.Sprintrotationy, 0.0, 1.0, 0.0);
//    		org.lwjgl.opengl.GL11.glRotatef(gunitem.Sprintrotationz, 0.0, 0.0, 1.0);
//    		org.lwjgl.opengl.GL11.glTranslatef(gunitem.Sprintoffsetx, gunitem.Sprintoffsety, gunitem.Sprintoffsetz);
//    	}else{
//		    if (!recoiled) {
//		        //�ˌ����1tick���̂݌Ă΂�܂�
//		    	renderinstance.glMatrixForRenderInEquipped(-0.2 - 0.005 * (1 - renderinstance.getSmoothing()));
//		    	org.lwjgl.opengl.GL11.glRotatef(gunitem.jump * (1 - renderinstance.getSmoothing()), 1.0, 0.0, 0.0);//�e�����ˏオ��B���̏�Ԃ��ƃ��f����0,0,0�𒆐S�ɉ�]�B
//
//
//		    } else {
//		        //�ʏ���
//		    	renderinstance.glMatrixForRenderInEquipped(-0.2);
//		    }
//		}
//        renderpartsNormal(renderinstance,gunitem,gunstack,model,entity,type,data);
//	}
//}
//org.lwjgl.opengl.GL11.glPopMatrix();//�I�����ɂ�����Ă�ōs����o�b�N�A�b�v����߂��܂��B
//}
//function renderunder(renderinstance,gunitem,gunstack,model,entity,type,data){
//    if(!gunitem.useundergunsmodel){
//        var underType = renderinstance.getUnderbarrelAttachType(entity,gunstack);//�e�g���̃A�^�b�`�����g�̎�ނ��擾
//        if(underType == 0) {
//            model.renderPart("mat9");
//        } else if(underType == 1){
//            model.renderPart("mat13");
//        } else if(underType == 5){
//            model.renderPart("mat10");
//        } else if(underType == 6){
//            model.renderPart("mat11");
//        } else {
//            model.renderPart("mat21");
//        }
//    }else{
//        var underType = renderinstance.getUnderbarrelAttachType(entity,gunstack);//�e�g���̃A�^�b�`�����g�̎�ނ��擾
//        if(underType == 0) {
//            model.renderPart("mat9");
//        } else if(underType == 1){
//            model.renderPart("mat13");
//        } else if(underType == 5) {
//            model.renderPart("mat10");
//        } else if(underType == 6) {
//            model.renderPart("mat11");
//        } else {
//            model.renderPart("mat21");
//        }
//        var underStack = renderinstance.getUnderbarrelAttachStack(entity,gunstack);
//        renderinstance.underRend_useunderGunModel(gunitem,underStack,type,data);
//    }
//}
//function rendesight(renderinstance,gunitem,gunstack,model,entity,type,data){
//    var sighttype = renderinstance.getAttach_SightType(entity,gunstack);
//    if(sighttype == 0){
//        model.renderPart("mat20");
//    }else if(sighttype == 1){
//        renderinstance.setLighting(240,240);
//        model.renderPart("mat4");
//        renderinstance.setLighting(renderinstance.getFirstpersonLighting()[0],renderinstance.getFirstpersonLighting()[1])
//    }else if(sighttype == 2){
//        model.renderPart("mat5");
//    }
//}
//function renderpartsOnReload(renderinstance,gunitem,gunstack,model,entity,type,data){
//    var cocking = renderinstance.getbooleanfromnbt("Cocking");//�R�b�L���O���Ă��邩�ǂ���
//    var cockingprogress = renderinstance.getintfromnbt("CockingTime") + renderinstance.getSmoothing() - 1;//�R�b�L���O�J�n����̎��ԁifloat�l�j
//    var recoiled = renderinstance.getbooleanfromnbt("Recoiled");//���R�C�������ǂ����i�e������u���˂�l�ɕ`�悷�邽�߂̃t���O�j
//    //var recoiledtime = renderinstance.getintfromnbt("RecoiledTime");//�g�r�s��
//    var boltprogress = renderinstance.getbytefromnbt("Bolt");//�{���g�A�R�b�L���O���Ȃ�AR�E�Z�~SR�n�ŋ��e�̃A�j���[�V�������������߁Bbyte�l(int�l�Ɉ�����͂�)
//    var isreloading = renderinstance.getbooleanfromnbt("IsReloading")//�����[�h�����ǂ���
//    var Crotex = renderinstance.getfloatfromnbt("rotex");//�V�����_�[�̉�]X
//    var Crotey = renderinstance.getfloatfromnbt("rotey");//�V�����_�[�̉�]Y
//    var Crotez = renderinstance.getfloatfromnbt("rotez");//�V�����_�[�̉�]Z
//    boltprogress -= renderinstance.getSmoothing();
//    var cycle = gunitem.cycle;//�e�̋��e�ɂ����鎞�ԁifor AR/semiSR�j
//    var boltoffsetcof;//�Ȟ��̉������Č����邽�߂̕��Bboltoffsetcof��0-1�𓮂��܂��B����ň�u�X���C�h���߂�l�q��`��o���܂��B
//    if (boltprogress < cycle / 2)//�{���g��ޒ�
//    	boltoffsetcof = cycle - boltprogress;
//    else
//    	boltoffsetcof = cycle - (cycle - boltprogress);
//    if (boltoffsetcof < 0) boltoffsetcof = 0;
//    var reloadprogress = renderinstance.getintfromnbt("RloadTime");
//    var sighttype = renderinstance.getAttach_SightType(entity,gunstack);//�T�C�g��ނ��擾
//    org.lwjgl.opengl.GL11.glScalef(renderinstance.modelscala,renderinstance.modelscala,renderinstance.modelscala);
//
//    model.renderPart("mat1");
//    //�}�K�W���������獷������
//    org.lwjgl.opengl.GL11.glTranslatef(0,-(gunitem.reloadtime - reloadprogress)*0.1,0);
//    model.renderPart("mat3");
//    org.lwjgl.opengl.GL11.glTranslatef(0,(gunitem.reloadtime - reloadprogress)*0.1,0);
//
//    org.lwjgl.opengl.GL11.glTranslatef(0.0, 0.0, -renderinstance.mat2offsetz);
//    model.renderPart("mat2");//�X���C�h
//    org.lwjgl.opengl.GL11.glTranslatef(0.0, 0.0, renderinstance.mat2offsetz);
//
//    org.lwjgl.opengl.GL11.glTranslatef(gunitem.mat25offsetx, gunitem.mat25offsety, gunitem.mat25offsetz);
//    org.lwjgl.opengl.GL11.glRotatef(gunitem.mat25rotationx, 1.0, 0.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(gunitem.mat25rotationy, 0.0, 1.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(gunitem.mat25rotationz, 0.0, 0.0, 1.0);
//    org.lwjgl.opengl.GL11.glTranslatef(-gunitem.mat25offsetx, -gunitem.mat25offsety, -gunitem.mat25offsetz);
//    org.lwjgl.opengl.GL11.glTranslatef(0, 0, gunitem.cocktime * 0.1);
//    model.renderPart("mat25");
//    org.lwjgl.opengl.GL11.glTranslatef(0, 0, -gunitem.cocktime * 0.1);
//    org.lwjgl.opengl.GL11.glTranslatef(gunitem.mat25offsetx, gunitem.mat25offsety, gunitem.mat25offsetz);
//    org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat25rotationx, 1.0, 0.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat25rotationy, 0.0, 1.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat25rotationz, 0.0, 0.0, 1.0);
//    org.lwjgl.opengl.GL11.glTranslatef(-gunitem.mat25offsetx, -gunitem.mat25offsety, -gunitem.mat25offsetz);
//
//    org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//    org.lwjgl.opengl.GL11.glRotatef(Crotey, 0.0, 1.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(Crotex, 1.0, 0.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(Crotez, 0.0, 0.0, 1.0);
//    org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//    model.renderPart("mat31");//�n���}�[
//    org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//    org.lwjgl.opengl.GL11.glRotatef(-Crotey, 0.0, 1.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(-Crotex, 1.0, 0.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(-Crotez, 0.0, 0.0, 1.0);
//    org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//
//    //�e�уJ�o�[��
//    org.lwjgl.opengl.GL11.glTranslatef(gunitem.mat22offsetx, gunitem.mat22offsety, gunitem.mat22offsetz);
//    org.lwjgl.opengl.GL11.glRotatef(gunitem.mat22rotationx, 1.0, 0.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(gunitem.mat22rotationy, 0.0, 1.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(gunitem.mat22rotationz, 0.0, 0.0, 1.0);
//    org.lwjgl.opengl.GL11.glTranslatef(-gunitem.mat22offsetx, -gunitem.mat22offsety, -gunitem.mat22offsetz);
//    model.renderPart("mat22");
//    org.lwjgl.opengl.GL11.glTranslatef(gunitem.mat22offsetx, gunitem.mat22offsety, gunitem.mat22offsetz);
//    org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat22rotationx, 1.0, 0.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat22rotationy, 0.0, 1.0, 0.0);
//    org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat22rotationz, 0.0, 0.0, 1.0);
//    org.lwjgl.opengl.GL11.glTranslatef(-gunitem.mat22offsetx, -gunitem.mat22offsety, -gunitem.mat22offsetz);
//
//    org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat32posx, renderinstance.mat32posy, renderinstance.mat32posz);//0,0.5,0
//    org.lwjgl.opengl.GL11.glRotatef(renderinstance.mat32rotey, 0.0, 1.0, 0.0);//90
//    org.lwjgl.opengl.GL11.glRotatef(renderinstance.mat32rotez, 0.0, 0.0, 1.0);
//    org.lwjgl.opengl.GL11.glRotatef(renderinstance.mat32rotex, 1.0, 0.0, 0.0);
//    org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat32posx, -renderinstance.mat32posy, -renderinstance.mat32posz);
//    model.renderPart("mat32");
//    org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat32posx, renderinstance.mat32posy, renderinstance.mat32posz);//0,0.5,0
//    org.lwjgl.opengl.GL11.glRotatef(-renderinstance.mat32rotey, 0.0, 1.0, 0.0);//90
//    org.lwjgl.opengl.GL11.glRotatef(-renderinstance.mat32rotez, 0.0, 0.0, 1.0);
//    org.lwjgl.opengl.GL11.glRotatef(-renderinstance.mat32rotex, 1.0, 0.0, 0.0);
//    org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat32posx, -renderinstance.mat32posy, -renderinstance.mat32posz);
//    rendesight(renderinstance,gunitem,gunstack,model,entity,type,data);
//    renderunder(renderinstance,gunitem,gunstack,model,entity,type,data);
//
//    org.lwjgl.opengl.GL11.glRotatef(180, 1.0, 0.0, 0.0);//�}�C�N���̘r�`��ƍ��W�n���Y���Ă���炵���̂ŏC���p
//    org.lwjgl.opengl.GL11.glTranslatef(0, -0.5, 0);
//    renderinstance.bindPlayertexture(entity);//�v���C���[�̃e�N�X�`�����g�p���邽�߂̃��\�b�h
//    renderinstance.renderarm(renderinstance.armrotationxl,
//                             renderinstance.armrotationyl,
//                             renderinstance.armrotationzl,
//                             renderinstance.armoffsetxl,
//                             renderinstance.armoffsetyl + (gunitem.reloadtime - reloadprogress)*0.1+1,//�r����Ɍ������ē������B
//                             renderinstance.armoffsetzl,//�����܂ō���
//                             renderinstance.armrotationxr,
//                             renderinstance.armrotationyr,
//                             renderinstance.armrotationzr,
//                             renderinstance.armoffsetxr,
//                             renderinstance.armoffsetyr,
//                             renderinstance.armoffsetzr);//�r��`�悷�邽�߂̃��\�b�h�B���Ɏg�����Ƃ��s�\�ł͖����ł�����ǉ����������Ă��邽�ߖʓ|�Ȃ��ƂɂȂ�܂��B
//                             //�ʒu���̑��͂Ƃ肠�����ݒ�l�����̂܂ܗ��p
//                             //��]�ʂЂ���Ƃ��ă��a�A���c�H
//}
//function renderpartsNormal(renderinstance,gunitem,gunstack,model,entity,type,data){
//    var cocking = renderinstance.getbooleanfromnbt("Cocking");//�R�b�L���O���Ă��邩�ǂ���
//    var cockingprogress = renderinstance.getintfromnbt("CockingTime") + renderinstance.getSmoothing() - 1;//�R�b�L���O�J�n����̎��ԁifloat�l�j
//    var recoiled = renderinstance.getbooleanfromnbt("Recoiled");//���R�C�������ǂ����i�e������u���˂�l�ɕ`�悷�邽�߂̃t���O�j
//    //var recoiledtime = renderinstance.getintfromnbt("RecoiledTime");//�g�r�s��
//    var boltprogress = renderinstance.getbytefromnbt("Bolt");//�{���g�A�R�b�L���O���Ȃ�AR�E�Z�~SR�n�ŋ��e�̃A�j���[�V�������������߁Bbyte�l(int�l�Ɉ�����͂�)
//    var isreloading = renderinstance.getbooleanfromnbt("IsReloading")//�����[�h�����ǂ���
//    var Crotex = renderinstance.getfloatfromnbt("rotex");//�V�����_�[�̉�]X
//    var Crotey = renderinstance.getfloatfromnbt("rotey");//�V�����_�[�̉�]Y
//    var Crotez = renderinstance.getfloatfromnbt("rotez");//�V�����_�[�̉�]Z
//    boltprogress -= renderinstance.getSmoothing();
//    var cycle = gunitem.cycle;//�e�̋��e�ɂ����鎞�ԁifor AR/semiSR�j
//    var boltoffsetcof;//�Ȟ��̉������Č����邽�߂̕��Bboltoffsetcof��0-1�𓮂��܂��B����ň�u�X���C�h���߂�l�q��`��o���܂��B
//    if (boltprogress < cycle / 2)//�{���g��ޒ�
//    	boltoffsetcof = cycle - boltprogress;
//    else
//    	boltoffsetcof = cycle - (cycle - boltprogress);
//    if (boltoffsetcof < 0) boltoffsetcof = 0;
//    org.lwjgl.opengl.GL11.glScalef(renderinstance.modelscala,renderinstance.modelscala,renderinstance.modelscala);
//    model.renderPart("mat1");
//    model.renderPart("mat3");
//    if (!recoiled){
//        org.lwjgl.opengl.GL11.glTranslatef(0.0, 0.0, -renderinstance.mat2offsetz * boltoffsetcof * (1 - renderinstance.getSmoothing()));
//        model.renderPart("mat2");//�X���C�h
//        //boltoffsetcof��1tick����0-1�Ԃ��ꉝ������̂ň�u��ނ���l�q��`��ł���B
//
//        org.lwjgl.opengl.GL11.glTranslatef(0.0, 0.0, renderinstance.mat2offsetz * boltoffsetcof * (1 - renderinstance.getSmoothing()));
//
//
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//        org.lwjgl.opengl.GL11.glRotatef(Crotey - renderinstance.mat31rotey * (1-renderinstance.getSmoothing()), 0.0, 1.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(Crotex - renderinstance.mat31rotex * (1-renderinstance.getSmoothing()), 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(Crotez - renderinstance.mat31rotez * (1-renderinstance.getSmoothing()), 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//        model.renderPart("mat31");//�V�����_�[
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//        org.lwjgl.opengl.GL11.glRotatef(-(Crotey - renderinstance.mat31rotey * (1-renderinstance.getSmoothing())), 0.0, 1.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(-(Crotex - renderinstance.mat31rotex * (1-renderinstance.getSmoothing())), 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(-(Crotez - renderinstance.mat31rotez * (1-renderinstance.getSmoothing())), 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat32posx, renderinstance.mat32posy, renderinstance.mat32posz);//0,0.5,0
//        org.lwjgl.opengl.GL11.glRotatef(renderinstance.mat32rotey*Math.abs(0.5-renderinstance.getSmoothing())*2, 0.0, 1.0, 0.0);//90
//        org.lwjgl.opengl.GL11.glRotatef(renderinstance.mat32rotez*Math.abs(0.5-renderinstance.getSmoothing())*2, 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glRotatef(renderinstance.mat32rotex*Math.abs(0.5-renderinstance.getSmoothing())*2, 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat32posx, -renderinstance.mat32posy, -renderinstance.mat32posz);
//        model.renderPart("mat32");//�n���}�[
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat32posx, renderinstance.mat32posy, renderinstance.mat32posz);//0,0.5,0
//        org.lwjgl.opengl.GL11.glRotatef(-renderinstance.mat32rotey*Math.abs(0.5-renderinstance.getSmoothing())*2, 0.0, 1.0, 0.0);//90
//        org.lwjgl.opengl.GL11.glRotatef(-renderinstance.mat32rotez*Math.abs(0.5-renderinstance.getSmoothing())*2, 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glRotatef(-renderinstance.mat32rotex*Math.abs(0.5-renderinstance.getSmoothing())*2, 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat32posx, -renderinstance.mat32posy, -renderinstance.mat32posz);
//    }else{
//        model.renderPart("mat2");//�X���C�h
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//        org.lwjgl.opengl.GL11.glRotatef(Crotey, 0.0, 1.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(Crotex, 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(Crotez, 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//        model.renderPart("mat31");//�V�����_�[
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//        org.lwjgl.opengl.GL11.glRotatef(-Crotey, 0.0, 1.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(-Crotex, 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(-Crotez, 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//        model.renderPart("mat32");//�n���}�[
//    }
//    if (cockingprogress <= 0) {
//    	model.renderPart("mat25");
//    } else {
//    	org.lwjgl.opengl.GL11.glTranslatef(gunitem.mat25offsetx, gunitem.mat25offsety, gunitem.mat25offsetz);
//    	org.lwjgl.opengl.GL11.glRotatef(gunitem.mat25rotationx, 1.0, 0.0, 0.0);
//    	org.lwjgl.opengl.GL11.glRotatef(gunitem.mat25rotationy, 0.0, 1.0, 0.0);
//    	org.lwjgl.opengl.GL11.glRotatef(gunitem.mat25rotationz, 0.0, 0.0, 1.0);
//    	org.lwjgl.opengl.GL11.glTranslatef(-gunitem.mat25offsetx, -gunitem.mat25offsety, -gunitem.mat25offsetz);
//    	if (cockingprogress > 0 && cockingprogress < ((gunitem.cocktime + renderinstance.getSmoothing() - 1) / 2)) {
//        	org.lwjgl.opengl.GL11.glTranslatef(0, 0, -(cockingprogress+renderinstance.getSmoothing()) * 0.1);
//        } else {
//        	org.lwjgl.opengl.GL11.glTranslatef(0, 0, (cockingprogress + renderinstance.getSmoothing() - gunitem.cocktime) * 0.1);
//        }
//        model.renderPart("mat25");//�Ȟ�
//        if (cockingprogress > 0 && cockingprogress < ((gunitem.cocktime + renderinstance.getSmoothing() - 1) / 2)) {
//        	org.lwjgl.opengl.GL11.glTranslatef(0, 0, (cockingprogress+renderinstance.getSmoothing()) * 0.1);
//        } else {
//        	org.lwjgl.opengl.GL11.glTranslatef(0, 0, -(cockingprogress + renderinstance.getSmoothing() - gunitem.cocktime) * 0.1);
//        }
//    	org.lwjgl.opengl.GL11.glTranslatef(gunitem.mat25offsetx, gunitem.mat25offsety, gunitem.mat25offsetz);
//    	org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat25rotationx, 1.0, 0.0, 0.0);
//    	org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat25rotationy, 0.0, 1.0, 0.0);
//    	org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat25rotationz, 0.0, 0.0, 1.0);
//    	org.lwjgl.opengl.GL11.glTranslatef(-gunitem.mat25offsetx, -gunitem.mat25offsety, -gunitem.mat25offsetz);
//    }
//    //�e�уJ�o�[��
//    model.renderPart("mat22");
//    rendesight(renderinstance,gunitem,gunstack,model,entity,type,data);
//
//    renderunder(renderinstance,gunitem,gunstack,model,entity,type,data);
//
//
//    org.lwjgl.opengl.GL11.glRotatef(180, 1.0, 0.0, 0.0);//�}�C�N���̘r�`��ƍ��W�n���Y���Ă���炵���̂ŏC���p
//    org.lwjgl.opengl.GL11.glTranslatef(0, -0.5, 0);
//    renderinstance.bindPlayertexture(entity);//�v���C���[�̃e�N�X�`�����g�p���邽�߂̃��\�b�h
//    renderinstance.renderarm(renderinstance.armrotationxl,
//                             renderinstance.armrotationyl,
//                             renderinstance.armrotationzl,
//                             renderinstance.armoffsetxl,
//                             renderinstance.armoffsetyl,
//                             renderinstance.armoffsetzl,
//                             renderinstance.armrotationxr,
//                             renderinstance.armrotationyr,
//                             renderinstance.armrotationzr,
//                             renderinstance.armoffsetxr,
//                             renderinstance.armoffsetyr,
//                             renderinstance.armoffsetzr);//�r��`�悷�邽�߂̃��\�b�h�B���Ɏg�����Ƃ��s�\�ł͖����ł�����ǉ����������Ă��邽�ߖʓ|�Ȃ��ƂɂȂ�܂��B
//                             //�ʒu���̑��͂Ƃ肠�����ݒ�l�����̂܂ܗ��p
//}
//function rendesight_third(renderinstance,gunitem,gunstack,model,entity,type,data){
//    var sighttype = renderinstance.getAttach_SightType(entity,gunstack);
//    if(sighttype == 0){
//        model.renderPart("mat20");
//    }else if(sighttype == 1){
//        renderinstance.setLighting(240,240);
//        model.renderPart("mat4");
//        renderinstance.setLighting(renderinstance.getThirdpersonLighting(entity)[0],renderinstance.getThirdpersonLighting(entity)[1])
//    }else if(sighttype == 2){
//        model.renderPart("mat5");
//    }
//}
//function renderThird(renderinstance,gunitem,gunstack,model,entity,type,data){
//    var cocking = renderinstance.getbooleanfromnbt("Cocking");//�R�b�L���O���Ă��邩�ǂ���
//    var cockingprogress = renderinstance.getintfromnbt("CockingTime") + renderinstance.getSmoothing() - 1;//�R�b�L���O�J�n����̎��ԁifloat�l�j
//    var recoiled = renderinstance.getbooleanfromnbt("Recoiled");//���R�C�������ǂ����i�e������u���˂�l�ɕ`�悷�邽�߂̃t���O�j
////    var recoiledtime = renderinstance.getintfromnbt("RecoiledTime");//�g�r�s��
//    var boltprogress = renderinstance.getbytefromnbt("Bolt");//�{���g�A�R�b�L���O���Ȃ�AR�E�Z�~SR�n�ŋ��e�̃A�j���[�V�������������߁Bbyte�l(int�l�Ɉ�����͂�)
//    var isreloading = renderinstance.getbooleanfromnbt("IsReloading")//�����[�h�����ǂ���
//    var Crotex = renderinstance.getfloatfromnbt("rotex");//�V�����_�[�̉�]X
//    var Crotey = renderinstance.getfloatfromnbt("rotey");//�V�����_�[�̉�]Y
//    var Crotez = renderinstance.getfloatfromnbt("rotez");//�V�����_�[�̉�]Z
//    boltprogress -= renderinstance.getSmoothing();
//    var cycle = gunitem.cycle;//�e�̋��e�ɂ����鎞�ԁifor AR/semiSR�j
//    var boltoffsetcof;//�Ȟ��̉������Č����邽�߂̕��Bboltoffsetcof��0-1�𓮂��܂��B����ň�u�X���C�h���߂�l�q��`��o���܂��B
//    if (boltprogress < cycle / 2)//�{���g��ޒ�
//    	boltoffsetcof = cycle - boltprogress;
//    else
//    	boltoffsetcof = cycle - (cycle - boltprogress);
//    if (boltoffsetcof < 0) boltoffsetcof = 0;
//    org.lwjgl.opengl.GL11.glPushMatrix();
//    var minecraft = renderinstance.getminecraft();
//    var sighttype = renderinstance.getAttach_SightType(entity,gunstack);
//    if(Packages.handmadeguns.mod_HandmadeGuns.islmmloaded && entity instanceof Packages.littleMaidMobX.LMM_EntityLittleMaid){
//        if (Packages.handmadeguns.mod_HandmadeGuns.cfg_RenderGunSizeLMM) {
//        	renderinstance.glMatrixForRenderInEntityLMM(0);
//            org.lwjgl.opengl.GL11.glScalef(renderinstance.modelscala - 0.25, renderinstance.modelscala - 0.25, renderinstance.modelscala - 0.25);
//            org.lwjgl.opengl.GL11.glTranslatef(0.5, 1.0, -0.3);
//        } else {
//        	renderinstance.glMatrixForRenderInEntity(0);
//        	org.lwjgl.opengl.GL11.glScalef(renderinstance.modelscala - 0.2, renderinstance.modelscala - 0.2, renderinstance.modelscala - 0.2);
//        }
//    } else if(renderinstance.is_entity_player(entity)){
//        renderinstance.glMatrixForRenderInEntityPlayer(0);
//        org.lwjgl.opengl.GL11.glTranslatef(0.0, 0.0, -0.3);
//        org.lwjgl.opengl.GL11.glScalef(renderinstance.modelscala, renderinstance.modelscala, renderinstance.modelscala);
//    }else{
//    	renderinstance.glMatrixForRenderInEntity(0);
//    	org.lwjgl.opengl.GL11.glScalef(renderinstance.modelscala - 0.2, renderinstance.modelscala - 0.2, renderinstance.modelscala - 0.2);
//    }
//    model.renderPart("mat1");
//    rendesight_third(renderinstance,gunitem,gunstack,model,entity,type,data);
//    renderunder(renderinstance,gunitem,gunstack,model,entity,type,data);
//    if(isreloading){
//    }else{
//        model.renderPart("mat3");
//    }
//    if (!recoiled){
//        org.lwjgl.opengl.GL11.glTranslatef(0.0, 0.0, -renderinstance.mat2offsetz * boltoffsetcof * (1 - renderinstance.getSmoothing()));
//        model.renderPart("mat2");//�X���C�h
//        //boltoffsetcof��1tick����0-1�Ԃ��ꉝ������̂ň�u��ނ���l�q��`��ł���B
//        //glTranslatef�ňړ��A�`�恨�p�[�c���ړ�����ĕ`�悳���B
//        //glTranslatef�ňړ��AglRotatef�ŉ�]���ړ���ŉ�]����ĕ`�悳���B
//        //glTranslatef�ňړ��AglRotatef�ŉ�]�AglTranslatef�ōŏ��Ɠ����ʋt�����Ɉړ�������_�𒆐S�ɉ�]�������ƂɂȂ�B
//        org.lwjgl.opengl.GL11.glTranslatef(0.0, 0.0, renderinstance.mat2offsetz * boltoffsetcof * (1 - renderinstance.getSmoothing()));
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//        org.lwjgl.opengl.GL11.glRotatef(Crotey - renderinstance.mat31rotey * (1-renderinstance.getSmoothing()), 0.0, 1.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(Crotex - renderinstance.mat31rotex * (1-renderinstance.getSmoothing()), 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(Crotez - renderinstance.mat31rotez * (1-renderinstance.getSmoothing()), 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//        model.renderPart("mat31");
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//        org.lwjgl.opengl.GL11.glRotatef(-(Crotey - renderinstance.mat31rotey * (1-renderinstance.getSmoothing())), 0.0, 1.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(-(Crotex - renderinstance.mat31rotex * (1-renderinstance.getSmoothing())), 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(-(Crotez - renderinstance.mat31rotez * (1-renderinstance.getSmoothing())), 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//    }else{
//        model.renderPart("mat2");
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//        org.lwjgl.opengl.GL11.glRotatef(Crotey, 0.0, 1.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(Crotex, 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(Crotez, 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//        model.renderPart("mat31");
//        org.lwjgl.opengl.GL11.glTranslatef(renderinstance.mat31posx, renderinstance.mat31posy, renderinstance.mat31posz);//0,0.7,0
//        org.lwjgl.opengl.GL11.glRotatef(-Crotey, 0.0, 1.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(-Crotex, 1.0, 0.0, 0.0);
//        org.lwjgl.opengl.GL11.glRotatef(-Crotez, 0.0, 0.0, 1.0);
//        org.lwjgl.opengl.GL11.glTranslatef(-renderinstance.mat31posx, -renderinstance.mat31posy, -renderinstance.mat31posz);
//    }
//    if (cockingprogress <= 0) {
//    	model.renderPart("mat25");
//    } else {
//    	org.lwjgl.opengl.GL11.glTranslatef(gunitem.mat25offsetx, gunitem.mat25offsety, gunitem.mat25offsetz);
//    	org.lwjgl.opengl.GL11.glRotatef(gunitem.mat25rotationx, 1.0, 0.0, 0.0);
//    	org.lwjgl.opengl.GL11.glRotatef(gunitem.mat25rotationy, 0.0, 1.0, 0.0);
//    	org.lwjgl.opengl.GL11.glRotatef(gunitem.mat25rotationz, 0.0, 0.0, 1.0);
//    	org.lwjgl.opengl.GL11.glTranslatef(-gunitem.mat25offsetx, -gunitem.mat25offsety, -gunitem.mat25offsetz);
//    	if (cockingprogress > 0 && cockingprogress < ((gunitem.cocktime + renderinstance.getSmoothing() - 1) / 2)) {
//    		org.lwjgl.opengl.GL11.glTranslatef(0, 0, -(cockingprogress+renderinstance.getSmoothing()) * 0.1);
//    	} else {
//    		org.lwjgl.opengl.GL11.glTranslatef(0, 0, (cockingprogress + renderinstance.getSmoothing() - gunitem.cocktime) * 0.1);
//    	}
//    	model.renderPart("mat25");
//    	if (cockingprogress > 0 && cockingprogress < ((gunitem.cocktime + renderinstance.getSmoothing() - 1) / 2)) {
//        	org.lwjgl.opengl.GL11.glTranslatef(0, 0, (cockingprogress+renderinstance.getSmoothing()) * 0.1);
//        } else {
//        	org.lwjgl.opengl.GL11.glTranslatef(0, 0, -(cockingprogress + renderinstance.getSmoothing() - gunitem.cocktime) * 0.1);
//        }
//    	org.lwjgl.opengl.GL11.glTranslatef(gunitem.mat25offsetx, gunitem.mat25offsety, gunitem.mat25offsetz);
//    	org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat25rotationx, 1.0, 0.0, 0.0);
//    	org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat25rotationy, 0.0, 1.0, 0.0);
//    	org.lwjgl.opengl.GL11.glRotatef(-gunitem.mat25rotationz, 0.0, 0.0, 1.0);
//    	org.lwjgl.opengl.GL11.glTranslatef(-gunitem.mat25offsetx, -gunitem.mat25offsety, -gunitem.mat25offsetz);
//    }
//    org.lwjgl.opengl.GL11.glPopMatrix();
//}