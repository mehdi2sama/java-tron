package stest.tron.wallet.dailybuild.delaytransaction;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.DeferredTransaction;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethed;
import stest.tron.wallet.common.client.utils.Sha256Hash;

@Slf4j
public class DelayTransaction003 {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final String testKey003 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] fromAddress = PublicMethed.getFinalAddress(testKey002);
  private final byte[] toAddress = PublicMethed.getFinalAddress(testKey003);

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private static final long now = System.currentTimeMillis();
  private static final long totalSupply = now;
  private static final String name = "Asset008_" + Long.toString(now);
  String description = "just-test";
  String url = "https://github.com/tronprotocol/wallet-cli/";
  Long delaySecond = 10L;

  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(1);
  private Long delayTransactionFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.delayTransactionFee");
  private Long cancleDelayTransactionFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.cancleDelayTransactionFee");
  ByteString assetId;



  Optional<TransactionInfo> infoById = null;
  Optional<DeferredTransaction> deferredTransactionById = null;
  Optional<Transaction> getTransactionById = null;


  ECKey ecKey = new ECKey(Utils.getRandom());
  byte[] assetOwnerAddress = ecKey.getAddress();
  String assetOwnerKey = ByteArray.toHexString(ecKey.getPrivKeyBytes());

  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] delayAccount2Address = ecKey2.getAddress();
  String delayAccount2Key = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

  ECKey ecKey3 = new ECKey(Utils.getRandom());
  byte[] receiverAssetAddress = ecKey3.getAddress();
  String receiverassetKey = ByteArray.toHexString(ecKey3.getPrivKeyBytes());

  ECKey ecKey4 = new ECKey(Utils.getRandom());
  byte[] delayAccount3Address = ecKey4.getAddress();
  String delayAccount3Key = ByteArray.toHexString(ecKey4.getPrivKeyBytes());

  ECKey ecKey5 = new ECKey(Utils.getRandom());
  byte[] receiverAccount4Address = ecKey5.getAddress();
  String receiverAccount4Key = ByteArray.toHexString(ecKey5.getPrivKeyBytes());

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  /**
   * constructor.
   */

  @BeforeClass(enabled = true)
  public void beforeClass() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = true, description = "Delay transfer asset")
  public void test1DelayTransferAsset() {
    //get account
    ecKey = new ECKey(Utils.getRandom());
    assetOwnerAddress = ecKey.getAddress();
    assetOwnerKey = ByteArray.toHexString(ecKey.getPrivKeyBytes());
    PublicMethed.printAddress(assetOwnerKey);
    ecKey3 = new ECKey(Utils.getRandom());
    receiverAssetAddress = ecKey3.getAddress();
    receiverassetKey = ByteArray.toHexString(ecKey3.getPrivKeyBytes());
    PublicMethed.printAddress(receiverassetKey);

    Assert.assertTrue(PublicMethed.sendcoin(assetOwnerAddress, 2048000000, fromAddress,
        testKey002, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    //Create test token.
    Long start = System.currentTimeMillis() + 2000;
    Long end = System.currentTimeMillis() + 1000000000;
    Assert.assertTrue(PublicMethed.createAssetIssue(assetOwnerAddress,
        name, totalSupply, 1, 1, start, end, 1, description, url,
        2000L, 2000L, 1L, 1L,
        assetOwnerKey, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Account assetOwnerAccount = PublicMethed.queryAccount(assetOwnerKey, blockingStubFull);
    assetId = assetOwnerAccount.getAssetIssuedID();

    //Delay transfer asset
    Long transferAssetAmount = 1L;
    final Long ownerAssetBalanceOfbeforeTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId, assetOwnerKey,blockingStubFull);
    final Long receiverAssetBalanceOfbeforeTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId,receiverassetKey,blockingStubFull);
    Assert.assertTrue(PublicMethed.transferAssetDelay(receiverAssetAddress, assetId.toByteArray(),
        transferAssetAmount, delaySecond,assetOwnerAddress, assetOwnerKey, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Long ownerAssetBalanceInDelayTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId,assetOwnerKey,blockingStubFull);
    final Long receiverAssetBalanceInDelayTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId,receiverassetKey,blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Long ownerAssetBalanceAfterTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId,assetOwnerKey,blockingStubFull);
    Long receiverAssetBalanceAfterTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId,receiverassetKey,blockingStubFull);


    Assert.assertEquals(ownerAssetBalanceOfbeforeTransferAsset,
        ownerAssetBalanceInDelayTransferAsset);
    Assert.assertTrue(receiverAssetBalanceOfbeforeTransferAsset
        == receiverAssetBalanceInDelayTransferAsset);
    Assert.assertTrue(ownerAssetBalanceInDelayTransferAsset - transferAssetAmount
        == ownerAssetBalanceAfterTransferAsset);
    Assert.assertTrue(receiverAssetBalanceAfterTransferAsset == transferAssetAmount);

  }


  @Test(enabled = true, description = "Cancel delay transfer asset")
  public void test2CancelDelayTransferAsset() {


    //Delay transfer asset
    Long transferAssetAmount = 1L;
    final Long ownerAssetBalanceOfbeforeTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId, assetOwnerKey,blockingStubFull);
    final Long receiverAssetBalanceOfbeforeTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId,receiverassetKey,blockingStubFull);

    String txid = PublicMethed.transferAssetDelayGetTxid(receiverAssetAddress, assetId.toByteArray(),
        transferAssetAmount, delaySecond,assetOwnerAddress, assetOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Assert.assertFalse(PublicMethed.cancelDeferredTransactionById(txid,receiverAssetAddress,receiverassetKey,blockingStubFull));
    Assert.assertTrue(PublicMethed.cancelDeferredTransactionById(txid,assetOwnerAddress,assetOwnerKey,blockingStubFull));
    Assert.assertFalse(PublicMethed.cancelDeferredTransactionById(txid,assetOwnerAddress,assetOwnerKey,blockingStubFull));

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Long ownerAssetBalanceAfterTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId,assetOwnerKey,blockingStubFull);
    Long receiverAssetBalanceAfterTransferAsset = PublicMethed
        .getAssetBalanceByAssetId(assetId,receiverassetKey,blockingStubFull);


    Assert.assertEquals(ownerAssetBalanceOfbeforeTransferAsset, ownerAssetBalanceAfterTransferAsset);
    Assert.assertTrue(receiverAssetBalanceAfterTransferAsset == receiverAssetBalanceOfbeforeTransferAsset);
    Assert.assertFalse(PublicMethed.cancelDeferredTransactionById(txid,assetOwnerAddress,assetOwnerKey,blockingStubFull));

  }




  /**
   * constructor.
   */

  @AfterClass(enabled = true)
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


