package cn.iocoder.yudao.adminserver.modules.system.service.errorcode;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.tool.framework.errorcode.core.dto.ErrorCodeAutoGenerateReqDTO;
import cn.iocoder.yudao.adminserver.modules.infra.enums.config.InfConfigTypeEnum;
import cn.iocoder.yudao.adminserver.modules.system.controller.errorcode.vo.SysErrorCodeCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.errorcode.vo.SysErrorCodeExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.errorcode.vo.SysErrorCodePageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.errorcode.vo.SysErrorCodeUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.errorcode.SysErrorCodeDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.errorcode.SysErrorCodeMapper;
import cn.iocoder.yudao.adminserver.modules.system.enums.errorcode.SysErrorCodeTypeEnum;
import cn.iocoder.yudao.adminserver.modules.system.service.errorcode.impl.SysErrorCodeServiceImpl;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.ERROR_CODE_DUPLICATE;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.ERROR_CODE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link SysErrorCodeServiceImpl} ??????????????????
*
* @author ????????????
*/
@Import(SysErrorCodeServiceImpl.class)
public class SysErrorCodeServiceTest extends BaseDbUnitTest {

    @Resource
    private SysErrorCodeServiceImpl errorCodeService;

    @Resource
    private SysErrorCodeMapper errorCodeMapper;

    @Mock
    private Logger log;

    @Test
    public void testCreateErrorCode_success() {
        // ????????????
        SysErrorCodeCreateReqVO reqVO = randomPojo(SysErrorCodeCreateReqVO.class);

        // ??????
        Long errorCodeId = errorCodeService.createErrorCode(reqVO);
        // ??????
        assertNotNull(errorCodeId);
        // ?????????????????????????????????
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(errorCodeId);
        assertPojoEquals(reqVO, errorCode);
        assertEquals(SysErrorCodeTypeEnum.MANUAL_OPERATION.getType(), errorCode.getType());
    }

    @Test
    public void testUpdateErrorCode_success() {
        // mock ??????
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO();
        errorCodeMapper.insert(dbErrorCode);// @Sql: ?????????????????????????????????
        // ????????????
        SysErrorCodeUpdateReqVO reqVO = randomPojo(SysErrorCodeUpdateReqVO.class, o -> {
            o.setId(dbErrorCode.getId()); // ??????????????? ID
        });

        // ??????
        errorCodeService.updateErrorCode(reqVO);
        // ????????????????????????
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(reqVO.getId()); // ???????????????
        assertPojoEquals(reqVO, errorCode);
        assertEquals(SysErrorCodeTypeEnum.MANUAL_OPERATION.getType(), errorCode.getType());
    }

    @Test
    public void testDeleteErrorCode_success() {
        // mock ??????
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO();
        errorCodeMapper.insert(dbErrorCode);// @Sql: ?????????????????????????????????
        // ????????????
        Long id = dbErrorCode.getId();

        // ??????
        errorCodeService.deleteErrorCode(id);
       // ????????????????????????
       assertNull(errorCodeMapper.selectById(id));
    }

    @Test
    public void testGetErrorCodePage() {
       // mock ??????
       SysErrorCodeDO dbErrorCode = initGetErrorCodePage();
       // ????????????
       SysErrorCodePageReqVO reqVO = new SysErrorCodePageReqVO();
       reqVO.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType());
       reqVO.setApplicationName("yudao");
       reqVO.setCode(1);
       reqVO.setMessage("yu");
       reqVO.setBeginCreateTime(buildTime(2020, 11, 1));
       reqVO.setEndCreateTime(buildTime(2020, 11, 30));

       // ??????
       PageResult<SysErrorCodeDO> pageResult = errorCodeService.getErrorCodePage(reqVO);
       // ??????
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbErrorCode, pageResult.getList().get(0));
    }

    /**
     * ????????? getErrorCodePage ?????????????????????
     */
    private SysErrorCodeDO initGetErrorCodePage() {
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> { // ???????????????
            o.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType());
            o.setApplicationName("yudaoyuanma");
            o.setCode(1);
            o.setMessage("yudao");
            o.setCreateTime(buildTime(2020, 11, 11));
        });
        errorCodeMapper.insert(dbErrorCode);
        // ?????? type ?????????
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setType(SysErrorCodeTypeEnum.MANUAL_OPERATION.getType())));
        // ?????? applicationName ?????????
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setApplicationName("yunai")));
        // ?????? code ?????????
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setCode(2)));
        // ?????? message ?????????
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setMessage("nai")));
        // ?????? createTime ?????????
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setCreateTime(buildTime(2020, 12, 12))));
        return dbErrorCode;
    }

    @Test
    public void testGetErrorCodeList() {
        // mock ??????
        SysErrorCodeDO dbErrorCode = initGetErrorCodePage();
        // ????????????
        SysErrorCodeExportReqVO reqVO = new SysErrorCodeExportReqVO();
        reqVO.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType());
        reqVO.setApplicationName("yudao");
        reqVO.setCode(1);
        reqVO.setMessage("yu");
        reqVO.setBeginCreateTime(buildTime(2020, 11, 1));
        reqVO.setEndCreateTime(buildTime(2020, 11, 30));

        // ??????
        List<SysErrorCodeDO> list = errorCodeService.getErrorCodeList(reqVO);
        // ??????
        assertEquals(1, list.size());
        assertPojoEquals(dbErrorCode, list.get(0));
    }

    @Test
    public void testValidateCodeDuplicate_codeDuplicateForCreate() {
        // ????????????
        Integer code = randomInteger();
        // mock ??????
        errorCodeMapper.insert(randomInfErrorCodeDO(o -> o.setCode(code)));

        // ?????????????????????
        assertServiceException(() -> errorCodeService.validateCodeDuplicate(code, null),
                ERROR_CODE_DUPLICATE);
    }

    @Test
    public void testValidateCodeDuplicate_codeDuplicateForUpdate() {
        // ????????????
        Long id = randomLongId();
        Integer code = randomInteger();
        // mock ??????
        errorCodeMapper.insert(randomInfErrorCodeDO(o -> o.setCode(code)));

        // ?????????????????????
        assertServiceException(() -> errorCodeService.validateCodeDuplicate(code, id),
                ERROR_CODE_DUPLICATE);
    }

    @Test
    public void testValidateErrorCodeExists_notExists() {
        assertServiceException(() -> errorCodeService.validateErrorCodeExists(null),
                ERROR_CODE_NOT_EXISTS);
    }

    /**
     * ?????? 1??????????????????????????????
     */
    @Test
    public void testAutoGenerateErrorCodes_01() {
        // ????????????
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class);
        // mock ??????

        // ??????
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // ??????
        SysErrorCodeDO errorCode = errorCodeMapper.selectOne(null);
        assertPojoEquals(generateReqDTO, errorCode);
        assertEquals(SysErrorCodeTypeEnum.AUTO_GENERATION.getType(), errorCode.getType());
    }

    /**
     * ?????? 2.1?????????????????????????????? SysErrorCodeTypeEnum.MANUAL_OPERATION ??????
     */
    @Test
    public void testAutoGenerateErrorCodes_021() {
        // mock ??????
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> o.setType(SysErrorCodeTypeEnum.MANUAL_OPERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // ????????????
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()));
        // mock ??????

        // ??????
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // ????????????????????????????????????
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(dbErrorCode, errorCode);
    }

    /**
     * ?????? 2.2?????????????????????????????? applicationName ?????????
     */
    @Test
    public void testAutoGenerateErrorCodes_022() {
        // mock ??????
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> o.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // ????????????
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()).setApplicationName(randomString()));
        // mock ??????

        // ??????
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // ????????????????????????????????????
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(dbErrorCode, errorCode);
    }

    /**
     * ?????? 2.3?????????????????????????????? message ??????
     */
    @Test
    public void testAutoGenerateErrorCodes_023() {
        // mock ??????
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> o.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // ????????????
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()).setApplicationName(dbErrorCode.getApplicationName())
                    .setMessage(dbErrorCode.getMessage()));
        // mock ??????

        // ??????
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // ????????????????????????????????????
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(dbErrorCode, errorCode);
    }

    /**
     * ?????? 2.3?????????????????????????????? message ????????????????????????
     */
    @Test
    public void testAutoGenerateErrorCodes_024() {
        // mock ??????
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> o.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // ????????????
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()).setApplicationName(dbErrorCode.getApplicationName()));
        // mock ??????

        // ??????
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // ???????????????
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(generateReqDTO, errorCode);
    }

    // ========== ???????????? ==========

    @SafeVarargs
    private static SysErrorCodeDO randomInfErrorCodeDO(Consumer<SysErrorCodeDO>... consumers) {
        Consumer<SysErrorCodeDO> consumer = (o) -> {
            o.setType(randomEle(InfConfigTypeEnum.values()).getType()); // ?????? key ?????????
        };
        return randomPojo(SysErrorCodeDO.class, ArrayUtils.append(consumer, consumers));
    }

}
