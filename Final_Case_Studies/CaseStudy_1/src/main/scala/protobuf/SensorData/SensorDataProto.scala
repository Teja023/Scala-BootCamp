// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package protobuf.SensorData

object SensorDataProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq.empty
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      protobuf.SensorData.SensorData
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
    scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
      """ChNTZW5zb3JSZWFkaW5nLnByb3RvEhNjYXNlU3R1ZHkxLnByb3RvYnVmIscBCg1TZW5zb3JSZWFkaW5nEikKCHNlbnNvcklkG
  AEgASgFQg3iPwoSCHNlbnNvcklkUghzZW5zb3JJZBIsCgl0aW1lc3RhbXAYAiABKANCDuI/CxIJdGltZXN0YW1wUgl0aW1lc3Rhb
  XASMgoLdGVtcGVyYXR1cmUYAyABKAJCEOI/DRILdGVtcGVyYXR1cmVSC3RlbXBlcmF0dXJlEikKCGh1bWlkaXR5GAQgASgCQg3iP
  woSCGh1bWlkaXR5UghodW1pZGl0eWIGcHJvdG8z"""
    ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}