with open('src/main/java/deluxe/client/module/combat/AttackAuraModule.java', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for idx, line in enumerate(lines):
    if 'this.wallAttackMode = this.addSetting(' in line:
        # replace the next line which has new ModeSetting(...)
        print(f"Setting line {idx}: {lines[idx+1]}")
        lines[idx+1] = '         new ModeSetting("Бить через блоки", "Разрешить наведение сквозь блоки.", "Нет", "Нет", "Обычные", "Неполноценные", "Shard", "Grim")\n'
        break

for idx, line in enumerate(lines):
    if 'if (AttackController.canAttackTarget(this.target, true, true, true, 0L, this.getRangeParameters())) {' in line:
        print(f"AttackController line {idx}")
        # Next line is if (!this.wallAttackMode.isSelected(...) || ...)
        lines[idx+1] = """                     Vec3d grimPoint = null;
                     if (this.wallAttackMode.isSelected("Grim")) {
                        grimPoint = getGrimVisiblePoint(this.target, Math.min(3.0, this.getEffectiveRange()));
                        if (grimPoint == null) {
                           return;
                        }
                        if (mc.player.networkHandler != null) {
                           float[] grimRots = EntityUtils.calculateRotationsToVec(grimPoint, mc.player.getEyePos());
                           mc.player.networkHandler.sendPacket(
                              new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround(
                                 grimRots[0], grimRots[1], mc.player.isOnGround(), mc.player.horizontalCollision
                              )
                           );
                        }
                     }

                     if ((!this.wallAttackMode.isSelected("Неполноценные") && !this.wallAttackMode.isSelected("\\ufffd")) || EntityUtils.canHitEntity(this.target, this.getEffectiveRange())) {
"""
        break

with open('src/main/java/deluxe/client/module/combat/AttackAuraModule.java', 'w', encoding='utf-8', newline='\n') as f:
    f.writelines(lines)

print("Patch applied to AttackAuraModule.java!")
