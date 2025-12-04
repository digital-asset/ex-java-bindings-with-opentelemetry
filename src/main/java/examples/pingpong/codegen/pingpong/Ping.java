package examples.pingpong.codegen.pingpong;

import static com.daml.ledger.javaapi.data.codegen.json.JsonLfEncoders.apply;

import com.daml.ledger.javaapi.data.ContractFilter;
import com.daml.ledger.javaapi.data.CreateAndExerciseCommand;
import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Int64;
import com.daml.ledger.javaapi.data.PackageVersion;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.javaapi.data.Unit;
import com.daml.ledger.javaapi.data.Value;
import com.daml.ledger.javaapi.data.codegen.Choice;
import com.daml.ledger.javaapi.data.codegen.ContractCompanion;
import com.daml.ledger.javaapi.data.codegen.ContractTypeCompanion;
import com.daml.ledger.javaapi.data.codegen.Created;
import com.daml.ledger.javaapi.data.codegen.Exercised;
import com.daml.ledger.javaapi.data.codegen.PrimitiveValueDecoders;
import com.daml.ledger.javaapi.data.codegen.Update;
import com.daml.ledger.javaapi.data.codegen.ValueDecoder;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoder;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfEncoder;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfEncoders;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfReader;
import examples.pingpong.codegen.da.internal.template.Archive;
import java.lang.Deprecated;
import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Ping extends Template {
  public static final Identifier TEMPLATE_ID = new Identifier("#ex-java-bindings", "PingPong", "Ping");

  public static final Identifier TEMPLATE_ID_WITH_PACKAGE_ID = new Identifier("3d938ce069cc748cbc084d50fff0f0744ba2924bc93e3a53b285966733236025", "PingPong", "Ping");

  public static final String PACKAGE_ID = "3d938ce069cc748cbc084d50fff0f0744ba2924bc93e3a53b285966733236025";

  public static final String PACKAGE_NAME = "ex-java-bindings";

  public static final PackageVersion PACKAGE_VERSION = new PackageVersion(new int[] {0, 0, 2});

  public static final Choice<Ping, RespondPong, Unit> CHOICE_RespondPong = 
      Choice.create("RespondPong", value$ -> value$.toValue(), value$ -> RespondPong.valueDecoder()
        .decode(value$), value$ -> PrimitiveValueDecoders.fromUnit.decode(value$),
        new RespondPong.JsonDecoder$().get(), JsonLfDecoders.unit, RespondPong::jsonEncoder,
        JsonLfEncoders::unit);

  public static final Choice<Ping, Archive, Unit> CHOICE_Archive = 
      Choice.create("Archive", value$ -> value$.toValue(), value$ -> Archive.valueDecoder()
        .decode(value$), value$ -> PrimitiveValueDecoders.fromUnit.decode(value$),
        new Archive.JsonDecoder$().get(), JsonLfDecoders.unit, Archive::jsonEncoder,
        JsonLfEncoders::unit);

  public static final ContractCompanion.WithoutKey<Contract, ContractId, Ping> COMPANION = 
      new ContractCompanion.WithoutKey<>(new ContractTypeCompanion.Package(Ping.PACKAGE_ID, Ping.PACKAGE_NAME, Ping.PACKAGE_VERSION),
        "examples.pingpong.codegen.pingpong.Ping", TEMPLATE_ID, ContractId::new,
        v -> Ping.templateValueDecoder().decode(v), Ping::fromJson, Contract::new,
        List.of(CHOICE_RespondPong, CHOICE_Archive));

  public final String sender;

  public final String receiver;

  public final Long count;

  public Ping(String sender, String receiver, Long count) {
    this.sender = sender;
    this.receiver = receiver;
    this.count = count;
  }

  @Override
  public Update<Created<ContractId>> create() {
    return new Update.CreateUpdate<ContractId, Created<ContractId>>(new CreateCommand(Ping.TEMPLATE_ID, this.toValue()), x -> x, ContractId::new);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseRespondPong} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseRespondPong(RespondPong arg) {
    return createAnd().exerciseRespondPong(arg);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseRespondPong} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseRespondPong() {
    return createAndExerciseRespondPong(new RespondPong());
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseArchive} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseArchive(Archive arg) {
    return createAnd().exerciseArchive(arg);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseArchive} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseArchive() {
    return createAndExerciseArchive(new Archive());
  }

  public static Update<Created<ContractId>> create(String sender, String receiver, Long count) {
    return new Ping(sender, receiver, count).create();
  }

  @Override
  public CreateAnd createAnd() {
    return new CreateAnd(this);
  }

  @Override
  protected ContractCompanion.WithoutKey<Contract, ContractId, Ping> getCompanion() {
    return COMPANION;
  }

  public static ValueDecoder<Ping> valueDecoder() throws IllegalArgumentException {
    return ContractCompanion.valueDecoder(COMPANION);
  }

  public DamlRecord toValue() {
    ArrayList<DamlRecord.Field> fields = new ArrayList<DamlRecord.Field>(3);
    fields.add(new DamlRecord.Field("sender", new Party(this.sender)));
    fields.add(new DamlRecord.Field("receiver", new Party(this.receiver)));
    fields.add(new DamlRecord.Field("count", new Int64(this.count)));
    return new DamlRecord(fields);
  }

  private static ValueDecoder<Ping> templateValueDecoder() throws IllegalArgumentException {
    return value$ -> {
      Value recordValue$ = value$;
      List<DamlRecord.Field> fields$ = PrimitiveValueDecoders.recordCheck(3,0, recordValue$);
      String sender = PrimitiveValueDecoders.fromParty.decode(fields$.get(0).getValue());
      String receiver = PrimitiveValueDecoders.fromParty.decode(fields$.get(1).getValue());
      Long count = PrimitiveValueDecoders.fromInt64.decode(fields$.get(2).getValue());
      return new Ping(sender, receiver, count);
    } ;
  }

  public static JsonLfDecoder<Ping> jsonDecoder() {
    return JsonLfDecoders.record(Arrays.asList("sender", "receiver", "count"), name -> {
          switch (name) {
            case "sender": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(0, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "receiver": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(1, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "count": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(2, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.int64);
            default: return null;
          }
        }
        , (Object[] args) -> new Ping(JsonLfDecoders.cast(args[0]), JsonLfDecoders.cast(args[1]), JsonLfDecoders.cast(args[2])));
  }

  public static Ping fromJson(String json) throws JsonLfDecoder.Error {
    return jsonDecoder().decode(new JsonLfReader(json));
  }

  public JsonLfEncoder jsonEncoder() {
    return JsonLfEncoders.record(
        JsonLfEncoders.Field.of("sender", apply(JsonLfEncoders::party, sender)),
        JsonLfEncoders.Field.of("receiver", apply(JsonLfEncoders::party, receiver)),
        JsonLfEncoders.Field.of("count", apply(JsonLfEncoders::int64, count)));
  }

  public static ContractFilter<Contract> contractFilter() {
    return ContractFilter.of(COMPANION);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (!(object instanceof Ping)) {
      return false;
    }
    Ping other = (Ping) object;
    return Objects.equals(this.sender, other.sender) &&
        Objects.equals(this.receiver, other.receiver) && Objects.equals(this.count, other.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.sender, this.receiver, this.count);
  }

  @Override
  public String toString() {
    return String.format("examples.pingpong.codegen.pingpong.Ping(%s, %s, %s)", this.sender,
        this.receiver, this.count);
  }

  public static final class ContractId extends com.daml.ledger.javaapi.data.codegen.ContractId<Ping> implements Exercises<ExerciseCommand> {
    public ContractId(String contractId) {
      super(contractId);
    }

    @Override
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, Ping, ?> getCompanion(
        ) {
      return COMPANION;
    }

    public static ContractId fromContractId(
        com.daml.ledger.javaapi.data.codegen.ContractId<Ping> contractId) {
      return COMPANION.toContractId(contractId);
    }
  }

  public static class Contract extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, Ping> {
    public Contract(ContractId id, Ping data, Set<String> signatories, Set<String> observers) {
      super(id, data, signatories, observers);
    }

    @Override
    protected ContractCompanion<Contract, ContractId, Ping> getCompanion() {
      return COMPANION;
    }

    public static Contract fromIdAndRecord(String contractId, DamlRecord record$,
        Set<String> signatories, Set<String> observers) {
      return COMPANION.fromIdAndRecord(contractId, record$, signatories, observers);
    }

    public static Contract fromCreatedEvent(CreatedEvent event) {
      return COMPANION.fromCreatedEvent(event);
    }
  }

  public interface Exercises<Cmd> extends com.daml.ledger.javaapi.data.codegen.Exercises.Archivable<Cmd> {
    default Update<Exercised<Unit>> exerciseRespondPong(RespondPong arg) {
      return makeExerciseCmd(CHOICE_RespondPong, arg);
    }

    default Update<Exercised<Unit>> exerciseRespondPong() {
      return exerciseRespondPong(new RespondPong());
    }

    default Update<Exercised<Unit>> exerciseArchive(Archive arg) {
      return makeExerciseCmd(CHOICE_Archive, arg);
    }

    default Update<Exercised<Unit>> exerciseArchive() {
      return exerciseArchive(new Archive());
    }
  }

  public static final class CreateAnd extends com.daml.ledger.javaapi.data.codegen.CreateAnd implements Exercises<CreateAndExerciseCommand> {
    CreateAnd(Template createArguments) {
      super(createArguments);
    }

    @Override
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, Ping, ?> getCompanion(
        ) {
      return COMPANION;
    }
  }

  /**
   * Proxies the jsonDecoder(...) static method, to provide an alternative calling synatx, which avoids some cases in generated code where javac gets confused
   */
  public static class JsonDecoder$ {
    public JsonLfDecoder<Ping> get() {
      return jsonDecoder();
    }
  }
}
